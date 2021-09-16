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
import com.ibm.commerce.order.objects.OrderAdjustmentAccessBean;
import com.ibm.commerce.order.objects.OrderItemAccessBean;
import com.ibm.commerce.ras.ECMessage;
/** 
 * This is the default implementation of the CSROrderAdjustmentCmd command.
 * Apply fixed discount on order level.
 * Calls the SetOrderLevelParameterCmd to set adjustment.
 * Calls the OrderCalculateCmd to calculate order.
 *
 */
public class CSROrderAdjustmentCmdImpl extends ControllerCommandImpl implements CSROrderAdjustmentCmd {
	private static final String CLASSNAME = CSROrderAdjustmentCmdImpl.class.getName();
	private static final Logger LOGGER = LoggingHelper.getLogger(CSROrderAdjustmentCmdImpl.class);

	private String orderId;
	private String ordAdjustment;

	/**
	 * Set the request properties 
	 */
	@Override
	public void setRequestProperties(TypedProperty reqProperties) throws ECException {
		String METHOD_NAME = "setRequestProperties";
		LOGGER.entering(CLASSNAME, METHOD_NAME);
		orderId = reqProperties.getString("orderId",null);
		ordAdjustment = reqProperties.getString("ordAdjustment",null);
		LOGGER.exiting(CLASSNAME, METHOD_NAME);
	}

	/**
	 * Calls executeSetOrderLevelAdjustmentCmd method to apply the adjustment
	 * 
	 */
	@Override
	public void performExecute() throws ECException {
		String METHOD_NAME = "performExecute";
		LOGGER.entering(CLASSNAME, METHOD_NAME);

			try {
				//set the order adjustment
				executeSetOrderLevelAdjustmentCmd();
			} catch (Exception e) {
				e.printStackTrace();
				throw new ECApplicationException(ECMessage._ERR_GENERIC, CLASSNAME,
						METHOD_NAME);
			}
		
		//set the response property
		TypedProperty responseProperty = new TypedProperty();
		responseProperty.put("orderId", orderId);
		super.setResponseProperties(responseProperty);
		LOGGER.exiting(CLASSNAME, METHOD_NAME);
	}
	
	/**
	 *  Calls SetOrderLevelParameterCmd to set adjustment.
	 * @throws ECException
	 * @throws Exception
	 */
	protected void executeSetOrderLevelAdjustmentCmd() throws ECException, Exception {
		String METHOD_NAME = "executeSetOrderLevelAdjustmentCmd";
		LOGGER.entering(CLASSNAME, METHOD_NAME);

		OrderDataBean orderBean = new OrderDataBean();
		orderBean.setSecurityCheck(false);
		orderBean.setOrderId(orderId);
		DataBeanManager.activate(orderBean, getCommandContext());
		
		OrderItemAccessBean abOrderItems[] = orderBean.getOrderItems();
		OrderAdjustmentAccessBean abOrderAdjustment = new OrderAdjustmentAccessBean();

		BigDecimal menuAdjustment = new BigDecimal(ordAdjustment);
		if (menuAdjustment.compareTo(orderBean.getDiscountAdjustmentTotal()) != 0) {
			SetOrderLevelParameterCmd setAdjustment = (SetOrderLevelParameterCmd) CommandFactory
					.createCommand("com.ibm.commerce.order.calculation.SetOrderLevelParameterCmd", getStoreId());
			if (setAdjustment == null)
				throw new ECApplicationException(ECMessage._ERR_CMD_CMD_NOT_FOUND, CLASSNAME,
						METHOD_NAME);
			setAdjustment.setOrder(orderBean);
			setAdjustment.setOrderItems(abOrderItems);
			setAdjustment.setCommandContext(getCommandContext());
			setAdjustment.setType(CalculationConstants.FIXED_REPLACEMENT);
			setAdjustment.setOverride(false);
			setAdjustment.setUsageId(CalculationConstants.USAGE_DISCOUNT);
			setAdjustment.setAmount(menuAdjustment.multiply(new BigDecimal("-1")));
			setAdjustment.execute();
			
			for (int i = 0; i < abOrderItems.length; i++) {
				Integer nPrepareFlags = abOrderItems[i].getPrepareFlagsInEntityType();
				nPrepareFlags = new Integer(nPrepareFlags.intValue() | 8);
				abOrderItems[i].setPrepareFlags(nPrepareFlags);
			}
			calculateOrder(orderBean);
		}
		
		

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
		
		if(StringUtils.isBlank(orderId)){
			throw new ECApplicationException(ECMessage._ERR_CMD_BAD_PARAMETER, CLASSNAME, METHOD_NAME,
					new Object[] { "orderId" });
		}
		
		if(StringUtils.isBlank(ordAdjustment)){
			throw new ECApplicationException(ECMessage._ERR_CMD_BAD_PARAMETER, CLASSNAME, METHOD_NAME,
					new Object[] { "ordAdjust"});
		}
		
		// ordAdjustment value is negative then throw the error
		if (Double.valueOf(ordAdjustment) < 0.0D){
			throw new ECApplicationException(ECMessage._ERR_INVALID_INPUT, CLASSNAME, METHOD_NAME,
					new Object[] { "ordAdjust" });
		}
		
	}
	/**
	 * Calls OrderCalculateCmd to calculate the order.
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
		orderCalculatCmd.setUsageIds(new Integer[] { CalculationConstants.USAGE_DISCOUNT });
		orderCalculatCmd.execute();
		LOGGER.exiting(CLASSNAME, METHOD_NAME);

	}

}
