package com.hcl.commerce.order.commands;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityExistsException;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import com.ibm.commerce.businessaudit.util.AuditLoggingHelper;
import com.ibm.commerce.command.CommandContext;
import com.ibm.commerce.command.CommandFactory;
import com.ibm.commerce.component.contextservice.ActivityToken;
import com.ibm.commerce.component.contextservice.BusinessContextServiceFactory;
import com.ibm.commerce.component.contextserviceimpl.BusinessContextInternalService;
import com.ibm.commerce.context.base.BaseContext;
import com.ibm.commerce.context.baseimpl.ContextHelper;
import com.ibm.commerce.datatype.TypedProperty;
import com.ibm.commerce.edp.commands.StoreAndValidatePaymentCmd;
import com.ibm.commerce.exception.ECApplicationException;
import com.ibm.commerce.exception.ECException;
import com.ibm.commerce.exception.ECSystemException;
import com.ibm.commerce.foundation.common.util.logging.OrderIntegrationLoggingHelper;
import com.ibm.commerce.order.commands.OrderCreateCmdImpl;
import com.ibm.commerce.order.commands.SetOrderOrganizationCmd;
import com.ibm.commerce.order.objects.OrderAccessBean;
import com.ibm.commerce.order.ras.WcOrderMessage;
import com.ibm.commerce.order.utils.OrderConstants;
import com.ibm.commerce.order.utils.OrderHelper;
import com.ibm.commerce.ras.ECMessage;
import com.ibm.commerce.ras.ECMessageHelper;
import com.ibm.commerce.ras.ECTrace;
import com.ibm.commerce.utils.TimestampHelper;

/**
 * This class is extended for set the business channel id while creating a new order
 *
 */
public class HCLOrderCreateCmdImpl extends OrderCreateCmdImpl {
	  private static final String CLASS_NAME = "com.hcl.commerce.order.commands.HCLOrderCreateCmdImpl";

		private static final String WEB_CHANNEL_ID="-1";
		private static final String CSR_CHANNEL_ID="-4";
		
	@Override
	public void performExecute() throws ECException {
        String methodName = "performExecute";
        ECTrace.entry(3L, "com.ibm.commerce.order.commands.OrderCreateCmdImpl", "performExecute");
        if(getCommandContext().getUserId().toString().compareTo("-1002") == 0)
            throw new ECApplicationException(ECMessage._ERR_USER_AUTHORITY, "com.ibm.commerce.order.commands.OrderCreateCmdImpl", "performExecute", "OrderCreateCmd");
        try
        {
            Map configMap = OrderHelper.getMaxPendingOrderByStoreMap();
            if(configMap != null)
            {
                Integer maxPendingOrderNum = (Integer)configMap.get(getStoreId());
                if(maxPendingOrderNum != null)
                {
                    OrderAccessBean orderFinder = new OrderAccessBean();
                    Enumeration pendingOrders = orderFinder.findByStatusMemberAndStore("P", getUserId(), getStoreId());
                    int currentPendingOrdersCount;
                    for(currentPendingOrdersCount = 0; pendingOrders.hasMoreElements(); currentPendingOrdersCount++)
                        pendingOrders.nextElement();

                    if(currentPendingOrdersCount >= maxPendingOrderNum.intValue())
                        throw new ECApplicationException(WcOrderMessage._ERR_EXCEED_MAXIMUM_PENDING_ORDER, "com.ibm.commerce.order.commands.OrderCreateCmdImpl", "performExecute");
                }
            }
            ECTrace.trace(3L, "com.ibm.commerce.order.commands.OrderCreateCmdImpl", "performExecute", (new StringBuilder("Create a new Order with userId=")).append(getTheUserId()).append(", storeId=").append(getStoreId()).toString());
            OrderAccessBean orderAB = new OrderAccessBean(getTheUserId(), getStoreId(), TimestampHelper.now());
            setOrderId(orderAB.getOrderId());
            orderAB.setTransferStatus(OrderConstants.ORDER_CAPTURING);
            orderAB.setType("ORD");
            orderAB.setStatus("P");
            orderAB.setDescription(getDescription());
            checkAndCSRLock(orderAB.getOrderIdInEntityType());
            CommandContext cmdContext = getCommandContext();
            BaseContext basectx = (BaseContext)cmdContext.getContext("com.ibm.commerce.context.base.BaseContext");
            String orderChannelId = basectx.getChannelId();
           // setting the channel id :start
            if(isCSR()){
            	orderChannelId=CSR_CHANNEL_ID;
            }else{
            	orderChannelId=WEB_CHANNEL_ID;
            }
           // setting the channel id :end
            
            if(orderChannelId != null && orderChannelId.trim().length() > 0)
            {
                Integer orderChannelIdInt = new Integer(orderChannelId);
                orderAB.setBuschnId(orderChannelIdInt);
            }
            ECTrace.trace(3L, "com.ibm.commerce.order.commands.OrderCreateCmdImpl", "performExecute", (new StringBuilder("A new Order has been created with orderId=")).append(getOrderId()).toString());
            updateCurrency(orderAB);
            SetOrderOrganizationCmd cmd = (SetOrderOrganizationCmd)CommandFactory.createCommand("com.ibm.commerce.order.commands.SetOrderOrganizationCmd", getStoreId());
            cmd.setCommandContext(getCommandContext());
            cmd.setOrder(orderAB);
            cmd.execute();
            List tmp_orderIds = new ArrayList();
            tmp_orderIds.add(getOrderId());
            OrderIntegrationLoggingHelper.trackOrderStatusInfo(tmp_orderIds, getClass().getName(), "performExecute");
            StoreAndValidatePaymentCmd createPaymentCmd = (StoreAndValidatePaymentCmd)CommandFactory.createCommand("com.ibm.commerce.edp.commands.StoreAndValidatePaymentCmd", getStoreId());
            createPaymentCmd.setCommandContext(getCommandContext());
            createPaymentCmd.setOrderId(new Long(getOrderId()));
            createPaymentCmd.setOrderAmount(new BigDecimal("0.0"));
            createPaymentCmd.setCurrency(orderAB.getCurrency());
            createPaymentCmd.execute();
            setEditor(orderAB);
            java.sql.Timestamp now = TimestampHelper.now();
            orderAB.setLastUpdate(now);
            raiseBusinessEvent(orderAB);
        }
        catch(EntityExistsException e)
        {
            throw new ECSystemException(ECMessage._ERR_CREATE_EXCEPTION, "com.ibm.commerce.order.commands.OrderCreateCmdImpl", "performExecute", ECMessageHelper.generateMsgParms(e.getMessage()), e);
        }
        catch(NoResultException e)
        {
            throw new ECSystemException(ECMessage._ERR_FINDER_EXCEPTION, "com.ibm.commerce.order.commands.OrderCreateCmdImpl", "performExecute", ECMessageHelper.generateMsgParms(e.getMessage()), e);
        }
        catch(PersistenceException e)
        {
            throw new ECSystemException(ECMessage._ERR_REMOTE_EXCEPTION, "com.ibm.commerce.order.commands.OrderCreateCmdImpl", "performExecute", ECMessageHelper.generateMsgParms(e.getMessage()), e);
        }
        catch(SQLException e)
        {
            throw new ECSystemException(ECMessage._ERR_SQL_EXCEPTION, "com.ibm.commerce.order.commands.OrderCreateCmdImpl", "performExecute", ECMessageHelper.generateMsgParms(e.getMessage()), e);
        }
        responseProperties = new TypedProperty();
        if(getUrl() != null)
        {
            responseProperties.put("redirecturl", getUrl());
            responseProperties.put("viewTaskName", "RedirectView");
        }
        responseProperties.put(getOutputOrderName(), getOrderId());
        if(getEditorId() != null)
            responseProperties.put("editorId", getEditorId());
        AuditLoggingHelper.getInstance().logPersonalDataAccess("com.ibm.commerce.order.commands.OrderCreateCmdImpl", "performExecute", getCommandContext().getRemoteAddr(), null, "created", null, "order");
        ECTrace.exit(3L, "com.ibm.commerce.order.commands.OrderCreateCmdImpl", "performExecute", ((Object) (ECMessageHelper.generateMsgParms(responseProperties))));
    }
	/**
	 * Check if User is CSR or not.
	 *
	 */
	 private boolean isCSR()
	    {
	        BaseContext context = (BaseContext)ContextHelper.getContext(getActivityToken(), "com.ibm.commerce.context.base.BaseContext");
	        return !context.getCallerId().equals(context.getRunAsId());
	    }
	 
	 /**
	  * Get the activity token
	  * @return iActivityToken
	  */
	 private ActivityToken getActivityToken()
	    {
	        if(iActivityToken == null)
	            iActivityToken = ((BusinessContextInternalService)BusinessContextServiceFactory.getBusinessContextService()).getActivityToken();
	        return iActivityToken;
	    }
	  private ActivityToken iActivityToken;

	 
}
