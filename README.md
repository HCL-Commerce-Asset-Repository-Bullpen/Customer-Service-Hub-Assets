
# CSR Adjustment
CSR can apply the fixed amount adjustment behalf of shopper.<br/>

## Integration Steps
1. Run CSRAdjustmentAccessControlPolicies.xml file from Dataload\acp folder.<br/>
2. Merge resources-ext.properties file changes from Rest\WebContent\WEB-INF\config folder.<br/>
3. Copy Controller Commands and Rest Handler files from WebSphereCommerceServerExtensionsLogic folder.<br/>
4. Restart the server.

## REST API
CSR invoke following REST API to apply the adjustment.
###### 1. Order Item Adjustment API
This service apply fixed amount discount on order item price.
```sh
URL: https://{{hostname}}/wcs/resources/storeId/{{storeId}}/csradjustment/orderitems
Method: POST
Request:
{
    "orderId":"",
    "orderItemId_1":"",
    "discount_1":""
}
```
###### 2. Order Adjustment API 
This service apply fixed amount discount on order total.
 ```sh
URL: https://{{hostname}}/wcs/resources/storeId/{{storeId}}/csradjustment/order
Method: POST
Request:
{
    "orderId":"",  
    "ordAdjustment":""  
}
```
###### 3. Shipping Adjustment API
This service apply fixed discount on shipping charges.
 ```sh
URL: https://{{hostname}}/wcs/resources/storeId/{{storeId}}/csradjustment/shipping
Method: POST
Request:
{
    "orderId":"", 
    "shipAdjustment":""
}
```


# Business ChannelId updapte for CSR user
 
CSR user place an order behalf of shopper then updating ORDERS.BUSCHN_ID to -4.

## Integration Steps ##

1. Run the BusinessChannel.sql file from Dataload/SQL folder.<br />
  If you already extended the OrderProcessCmd and OrderCreateCmd then you don't need to run this file.
   
2. Copy commands Files from WebSphereCommerceServerExtensionsLogic.<br />
   You have extended OrderProcessCmdImpl then merge your changes from HCLExtOrderProcessCmdImpl.java file otherwise copy HCLExtOrderProcessCmdImpl.java file in your WebSphereCommerceServerExtensionsLogic.<br />
	 
   You have extended OrderCreateCmdImpl then merge your changes from HCLOrderCreateCmdImpl.java file otherwise copy HCLOrderCreateCmdImpl.java file in your WebSphereCommerceServerExtensionsLogic. <br />

3. Restart the server.
   


