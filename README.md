# Business ChannelId updapte for CSR user
 
CSR user place an order behalf of shopper then updating ORDERS.BUSCHN_ID to -4.

## Integration Steps ##

1. Run the BusinessChannel.sql file from Dataload/SQL folder.<br />
  If you already extended the OrderProcessCmd and OrderCreateCmd then you don't need to run this file.
   
2. Copy commands Files from WebSphereCommerceServerExtensionsLogic.<br />
   You have extended OrderProcessCmdImpl then merge your changes from HCLExtOrderProcessCmdImpl.java file otherwise copy HCLExtOrderProcessCmdImpl.java file in your WebSphereCommerceServerExtensionsLogic.<br />
	 
   You have extended OrderCreateCmdImpl then merge your changes from HCLOrderCreateCmdImpl.java file otherwise copy HCLOrderCreateCmdImpl.java file in your WebSphereCommerceServerExtensionsLogic. <br />

3. Restart the server.
   
