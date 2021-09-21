package com.hcl.commerce.order.commands;

import java.math.BigDecimal;
import java.util.Hashtable;
import java.util.logging.Level;
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
import com.ibm.commerce.order.calculation.SetOrderItemLevelParameterCmd;
import com.ibm.commerce.order.commands.OrderCalculateCmd;
import com.ibm.commerce.order.objects.OrderAccessBean;
import com.ibm.commerce.order.objects.OrderItemAccessBean;
import com.ibm.commerce.order.utils.ResolveParameter;
import com.ibm.commerce.ras.ECMessage;

/** 
 * This is the default implementation of the CSROrderItemAdjustmentCmd command.
 * Apply fixed discount on order item level.
 * Calls the SetOrderItemLevelParameterCmd to set order item adjustment.
 * Calls the OrderCalculateCmd to calculate order.
 *
 */
public class CSROrderItemAdjustmentCmdImpl extends ControllerCommandImpl implements CSROrderItemAdjustmentCmd {
	private static final String CLASSNAME = CSROrderItemAdjustmentCmdImpl.class.getName();
	private static final Logger LOGGER = LoggingHelper.getLogger(CSROrderItemAdjustmentCmdImpl.class);

	private String orderId;
	private Hashtable<String, String> orderitemIds;
	private Hashtable<String, String> discounts;

	/**
	 * Set the request property file
	 */
	@Override
	public void setRequestProperties(TypedProperty reqProperties) throws ECException {
		String METHOD_NAME = "setRequestProperties";
		LOGGER.entering(CLASSNAME, METHOD_NAME);
		orderId = reqProperties.getString("orderId");
		orderitemIds = ResolveParameter.resolveValues("orderItemId", reqProperties, false);
		discounts = ResolveParameter.resolveValues("discount", reqProperties, false);
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

		if (orderitemIds.size() == 0) {
			throw new ECApplicationException(ECMessage._ERR_CMD_BAD_PARAMETER, CLASSNAME, METHOD_NAME,
					new Object[] { "orderItemId" });
		}

		if (discounts.size() == 0) {
			throw new ECApplicationException(ECMessage._ERR_CMD_BAD_PARAMETER, CLASSNAME, METHOD_NAME,
					new Object[] { "discount" });
		}
		LOGGER.exiting(CLASSNAME, METHOD_NAME);
	}

	/**
	 * Calls executeSetOrderItemsAdjustmentCmd method to apply the order items
	 * adjustment
	 */
	@Override
	public void performExecute() throws ECException {
		String METHOD_NAME = "performExecute";
		LOGGER.entering(CLASSNAME, METHOD_NAME);

		try {
			executeSetOrderItemsAdjustmentCmd();
		}catch(ECApplicationException e){
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ECApplicationException(ECMessage._ERR_GENERIC, CLASSNAME, METHOD_NAME);
		}

		// Set the response properties
		TypedProperty resprop = new TypedProperty();
		resprop.put("orderId", orderId);
		setResponseProperties(resprop);
	}

	/**
	 * Calls SetOrderLevelParameterCmd to set order items adjustment.
	 * 
	 * @throws ECException
	 * @throws Exception
	 */
	protected void executeSetOrderItemsAdjustmentCmd() throws ECException, Exception {
		String METHOD_NAME = "executeSetOrderItemsAdjustmentCmd";
		LOGGER.entering(CLASSNAME, METHOD_NAME);

		if (orderitemIds.size() > 0) {
			OrderDataBean orderBean = new OrderDataBean();
			orderBean.setSecurityCheck(false);
			orderBean.setOrderId(orderId);
			DataBeanManager.activate(orderBean, getCommandContext());
			OrderItemAccessBean abOrderItems[] = orderBean.getOrderItems();
			for (OrderItemAccessBean orderiAB : abOrderItems) {
				for (int i = 1; i <= orderitemIds.size(); i++) {
					if (orderiAB.getOrderItemId().equals(orderitemIds.get(i))) {
						String oiDiscount = discounts.get(i);
						
						if (oiDiscount != null && oiDiscount.length() != 0) {
							// Discount value is negative then throw the error
							if (Double.valueOf(oiDiscount) < 0.0D){
								throw new ECApplicationException(ECMessage._ERR_INVALID_INPUT, CLASSNAME, METHOD_NAME,
										new Object[] { "discount" });
							}
							
							BigDecimal menuAdjustment = new BigDecimal(oiDiscount);
								LOGGER.logp(Level.FINEST, CLASSNAME, METHOD_NAME,
										(new StringBuilder("menuAdjustment = ")).append(menuAdjustment).toString());
								SetOrderItemLevelParameterCmd setAdjustment = (SetOrderItemLevelParameterCmd) CommandFactory
										.createCommand(
												"com.ibm.commerce.order.calculation.SetOrderItemLevelParameterCmd",
												getStoreId());
								if (setAdjustment == null)
									throw new ECApplicationException(ECMessage._ERR_CMD_CMD_NOT_FOUND,
											getClass().getName(), "executeOrderItemUpdate");
								setAdjustment.setOrder(orderBean);
								setAdjustment.setOrderItem(orderiAB);
								setAdjustment.setCommandContext(getCommandContext());
								setAdjustment.setType(CalculationConstants.FIXED_ADJUSTMENT);
								setAdjustment.setOverride(false);
								setAdjustment.setUsageId(CalculationConstants.USAGE_DISCOUNT);
								BigDecimal oiTotalProduct = orderiAB.getTotalProductInEntityType();
								if (menuAdjustment.compareTo(oiTotalProduct) > 0)
									setAdjustment.setAmount(oiTotalProduct.multiply(new BigDecimal("-1")));
								else
									setAdjustment.setAmount(menuAdjustment.multiply(new BigDecimal("-1")));
								setAdjustment.execute();
								Integer nPrepareFlags = orderiAB.getPrepareFlagsInEntityType();
								nPrepareFlags = new Integer(nPrepareFlags.intValue() | 8);
								orderiAB.setPrepareFlags(nPrepareFlags);
							
						}
						break;
					}
				}
			}
			calculateOrder(orderBean);

		}

		LOGGER.exiting(CLASSNAME, METHOD_NAME);
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
		// usageid -1 for discount
		orderCalculatCmd.setUsageIds(new Integer[] { CalculationConstants.USAGE_DISCOUNT });
		orderCalculatCmd.execute();
		LOGGER.exiting(CLASSNAME, METHOD_NAME);

	}

}
