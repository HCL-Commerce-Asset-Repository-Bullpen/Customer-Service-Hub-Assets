package com.hcl.commerce.order.commands;

import com.ibm.commerce.command.ControllerCommand;

public interface CSRShippingAdjustmentCmd extends ControllerCommand {
	public static final String defaultCommandClassName =CSRShippingAdjustmentCmdImpl.class.getName();

}
