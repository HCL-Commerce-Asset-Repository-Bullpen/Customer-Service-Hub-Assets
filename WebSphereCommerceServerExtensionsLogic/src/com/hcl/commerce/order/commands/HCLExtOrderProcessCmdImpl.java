package com.hcl.commerce.order.commands;

import com.ibm.commerce.exception.ECException;
import com.ibm.commerce.order.commands.OrderProcessCmdImpl;
import com.ibm.commerce.order.objects.OrderAccessBean;
/**
 * This command extends to set the business channel id.
 *
 */
public class HCLExtOrderProcessCmdImpl extends OrderProcessCmdImpl {
	
	private static final int WEB_CHANNEL_ID=-1;
	private static final int CSR_CHANNEL_ID=-4;
	
/**
 *  This method set the channel id.  
 */
	@Override
	public void performExecute() throws ECException {
		
	super.performExecute();
		
	OrderAccessBean[] orderABs =getOrders();
	for(OrderAccessBean orderAB:orderABs){
		if(isCSR()){
			orderAB.setBuschnId(CSR_CHANNEL_ID);
		}else{
			orderAB.setBuschnId(WEB_CHANNEL_ID);
		}
		
	}
	}

/**
 * Check user is csr or not.
 * @return
 */
private boolean isCSR(){
	if(getCommandContext().getUserId().equals(getCommandContext().getCallerId())){
		return false;
	}else{
		return true;
	}
}
}