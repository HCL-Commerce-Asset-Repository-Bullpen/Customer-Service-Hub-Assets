-- Update your store storeId in this field <storeId> 
INSERT INTO CMDREG (STOREENT_ID,INTERFACENAME,CLASSNAME,PROPERTIES,TARGET,OPTCOUNTER) VALUES(<storeId>,'com.ibm.commerce.order.commands.OrderProcessCmd','com.hcl.commerce.order.commands.HCLExtOrderProcessCmdImpl','retriable=1','Local',0);

-- Update your store storeId in this field <storeId> 
INSERT INTO CMDREG (STOREENT_ID,INTERFACENAME,CLASSNAME,PROPERTIES,TARGET,OPTCOUNTER) VALUES(<storeId>,'com.ibm.commerce.order.commands.OrderCreateCmd','com.hcl.commerce.order.commands.HCLOrderCreateCmdImpl','retriable=1','Local',0);
