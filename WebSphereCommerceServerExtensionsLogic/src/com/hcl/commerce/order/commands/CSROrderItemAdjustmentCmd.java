package com.hcl.commerce.order.commands;

import com.ibm.commerce.command.ControllerCommand;

public interface CSROrderItemAdjustmentCmd extends ControllerCommand {
	public static final String defaultCommandClassName =CSROrderItemAdjustmentCmdImpl.class.getName();
}
