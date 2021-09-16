package com.hcl.commerce.order.commands;

import java.math.BigDecimal;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.ibm.commerce.beans.DataBeanManager;
import com.ibm.commerce.command.CommandFactory;
import com.ibm.commerce.command.ControllerCommandImpl;
import com.ibm.commerce.datatype.TypedProperty;
import com.ibm.commerce.exception.ECApplicationException;
import com.ibm.commerce.exception.ECException;
import com.ibm.commerce.foundation.logging.LoggingHelper;
import com.ibm.commerce.order.beans.OrderDataBean;
import com.ibm.commerce.order.calculation.CalculationConstants;
import com.ibm.commerce.order.calculation.SetOrderLevelParameterCmd;
import com.ibm.commerce.order.commands.OrderCalculateCmd;
import com.ibm.commerce.order.objects.OrderAccessBean;
import com.ibm.commerce.order.objects.OrderItemAccessBean;
import com.ibm.commerce.ras.ECMessage;

/**
 * This is the default implementation of the CSRShippingAdjustmentCmdImpl
 * command. Apply Shipping discount. Calls the SetOrderLevelParameterCmd to set
 * adjustment. Calls the OrderCalculateCmd to calculate order.
 * 
 * @author maheshkumar.mantri
 *
 */
public class CSRShippingAdjustmentCmdImpl extends ControllerCommandImpl implements CSRShippingAdjustmentCmd {

	private static final String CLASSNAME = CSRShippingAdjustmentCmdImpl.class.getName();
	private static final Logger LOGGER = LoggingHelper.getLogger(CSRShippingAdjustmentCmdImpl.class);

	private String orderId;
	private String shipAdjustment;

	/**
	 * Set the request properties
	 */
	@Override
	public void setRequestProperties(TypedProperty reqProperties) throws ECException {
		String METHOD_NAME = "setRequestProperties";
		LOGGER.entering(CLASSNAME, METHOD_NAME);
		orderId = reqProperties.getString("orderId", null);
		shipAdjustment = reqProperties.getString("shipAdjustment", null);
		LOGGER.exiting(CLASSNAME, METHOD_NAME);
	}

	/**
	 * Validate the request parameter.
	 */
	@Override
	public void validateParameters() throws ECException {
		String METHOD_NAME = "validateParameters";
		LOGGER.entering(CLASSNAME, METHOD_NAME);
		// It allow CSR user to access this command behalf of user.
		if (getCommandContext().getUserId().equals(getCommandContext().getCallerId())) {
			throw new ECApplicationException(ECMessage._ERR_USER_AUTHORITY, CLASSNAME, METHOD_NAME,
					new Object[] { getCommandName() });
		}

		if (StringUtils.isBlank(orderId)) {
			throw new ECApplicationException(ECMessage._ERR_CMD_BAD_PARAMETER, CLASSNAME, METHOD_NAME,
					new Object[] { "orderId" });
		}

		if (StringUtils.isBlank(shipAdjustment)) {
			throw new ECApplicationException(ECMessage._ERR_CMD_BAD_PARAMETER, CLASSNAME, METHOD_NAME,
					new Object[] { "shipAdjustment" });
		}
		// shipAdjustment value is negative then throw the error
		if (Double.valueOf(shipAdjustment) < 0.0D){
			throw new ECApplicationException(ECMessage._ERR_INVALID_INPUT, CLASSNAME, METHOD_NAME,
					new Object[] { "shipAdjustment" });
		}
	}

	/**
	 * Calls executeSetShippingAdjustmentCmd method to apply the shipping
	 * adjustment
	 * 
	 */
	@Override
	public void performExecute() throws ECException {
		String METHOD_NAME = "performExecute";
		LOGGER.entering(CLASSNAME, METHOD_NAME);
		try {
			// Set the shipping adjustment
			executeSetShippingAdjustmentCmd();

		} catch (Exception e) {
			e.printStackTrace();
			throw new ECApplicationException(ECMessage._ERR_GENERIC, CLASSNAME, METHOD_NAME);
		}
		// set the response property
		TypedProperty responseProperty = new TypedProperty();
		responseProperty.put("orderId", orderId);
		super.setResponseProperties(responseProperty);

		LOGGER.exiting(CLASSNAME, METHOD_NAME);

	}

	/**
	 * Calls SetOrderLevelParameterCmd to set shipping adjustment.
	 * 
	 * @throws ECException
	 * @throws Exception
	 */
	protected void executeSetShippingAdjustmentCmd() throws ECException, Exception {
		String METHOD_NAME = "executeSetShippingAdjustmentCmd";
		LOGGER.entering(CLASSNAME, METHOD_NAME);
		OrderDataBean orderBean = new OrderDataBean();
		orderBean.setSecurityCheck(false);
		orderBean.setOrderId(orderId);
		DataBeanManager.activate(orderBean, getCommandContext());

		OrderItemAccessBean abOrderItems[] = orderBean.getOrderItems();
		BigDecimal shippingChargesTotal = orderBean.getTotalShippingChargeInEntityType();
		BigDecimal shippingAdjustment = new BigDecimal(shipAdjustment);

		// checking shipping adjustment<=shipping charges total
		// shippingAdjustment>shippingChargesTotal then set shipping adjustment value as shipping charges total
				if (shippingAdjustment.compareTo(shippingChargesTotal) > 0){
					shippingAdjustment=shippingChargesTotal;
				}
			SetOrderLevelParameterCmd setAdjustment = (SetOrderLevelParameterCmd) CommandFactory
					.createCommand("com.ibm.commerce.order.calculation.SetOrderLevelParameterCmd", getStoreId());
			if (setAdjustment == null)
				throw new ECApplicationException(ECMessage._ERR_CMD_CMD_NOT_FOUND, CLASSNAME, METHOD_NAME);
			setAdjustment.setOrder(orderBean);
			setAdjustment.setOrderItems(abOrderItems);
			setAdjustment.setCommandContext(getCommandContext());
			setAdjustment.setType(CalculationConstants.FIXED_REPLACEMENT);
			setAdjustment.setOverride(false);
			setAdjustment.setUsageId(CalculationConstants.USAGE_SHIPPING_ADJUSTMENT);
			setAdjustment.setAmount(shippingAdjustment.multiply(new BigDecimal("-1")));
			setAdjustment.execute();

			for (int i = 0; i < abOrderItems.length; i++) {
				Integer nPrepareFlags = abOrderItems[i].getPrepareFlagsInEntityType();
				nPrepareFlags = new Integer(nPrepareFlags.intValue() | 8);
				abOrderItems[i].setPrepareFlags(nPrepareFlags);
			}
			calculateOrder(orderBean);
		
		LOGGER.entering(CLASSNAME, METHOD_NAME);
	}

	/**
	 * Calls OrderCalculateCmd to calculate the order.
	 * 
	 * @param orderBean
	 * @throws ECException
	 */
	private void calculateOrder(OrderDataBean orderBean) throws ECException {
		String METHOD_NAME = "calculateOrder";
		LOGGER.entering(CLASSNAME, METHOD_NAME);
		OrderCalculateCmd orderCalculatCmd = (OrderCalculateCmd) CommandFactory.createCommand(OrderCalculateCmd.NAME,
				getStoreId());
		orderCalculatCmd.setCommandContext(getCommandContext());
		orderCalculatCmd.setAccCheck(false);
		orderCalculatCmd.setOrders(new OrderAccessBean[] { orderBean });
		//  -7 for shipping adjustment
		orderCalculatCmd.setUsageIds(
				new Integer[] { CalculationConstants.USAGE_SHIPPING_ADJUSTMENT });
		orderCalculatCmd.execute();
		LOGGER.exiting(CLASSNAME, METHOD_NAME);

	}
}
