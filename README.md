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
