package com.hcl.commerce.order.commands;

import com.ibm.commerce.command.ControllerCommand;

public interface CSROrderAdjustmentCmd extends ControllerCommand {
	
	public static final String defaultCommandClassName =CSROrderAdjustmentCmdImpl.class.getName();

}
