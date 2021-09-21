package com.hcl.commerce.rest.order.handler;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.commerce.datatype.TypedProperty;
import com.ibm.commerce.rest.classic.core.AbstractConfigBasedClassicHandler;
import com.ibm.commerce.rest.javadoc.ResponseSchema;
/**
 * This handler is created to apply CSR adjustment.
 * CSR invoke rest API to apply order, orderitems and shipping level adjustment.
 *
 */

@Path("storeId/{storeId}/csradjustment")
public class CSROrderAdjustmentHandler extends AbstractConfigBasedClassicHandler {

	private static final String RESOURCE_NAME = "csradjustment";
	private static final String CSR_ORDER_ITEMS = "orderitems";
	private static final String CSR_ORDERS = "order";
	private static final String CSR_ORDER_SHIPPING = "shipping";
	private static final String CSR_ORDER_ITEMS_ADJUSTMENT_CMD = "com.hcl.commerce.order.commands.CSROrderItemAdjustmentCmd";
	private static final String CSR_ORDERS_ADJUSTMENT_CMD = "com.hcl.commerce.order.commands.CSROrderAdjustmentCmd";
	private static final String CSR_ORDERS_SHIPPING_ADJUSTMENT_CMD = "com.hcl.commerce.order.commands.CSRShippingAdjustmentCmd";

	@Override
	public String getResourceName() {

		return RESOURCE_NAME;
	}

	/**
	 * This method call CSROrderItemAdjustmentCmd to apply CSR order line level adjustment.
	 * @param storeId
	 * @param responseFormat
	 * @return
	 * @throws Exception
	 */
	@Path(CSR_ORDER_ITEMS)
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_XHTML_XML,
			MediaType.APPLICATION_ATOM_XML })
	@ResponseSchema(parameterGroup = RESOURCE_NAME, responseCodes = {
			@com.ibm.commerce.rest.javadoc.ResponseCode(code = 200, reason = "The requested completed successfully."),
			@com.ibm.commerce.rest.javadoc.ResponseCode(code = 400, reason = "Bad request. Some of the inputs provided to the request aren't valid."),
			@com.ibm.commerce.rest.javadoc.ResponseCode(code = 401, reason = "Not authenticated. The user session isn't valid."),
			@com.ibm.commerce.rest.javadoc.ResponseCode(code = 403, reason = "The user isn't authorized to perform the specified request."),
			@com.ibm.commerce.rest.javadoc.ResponseCode(code = 404, reason = "The specified resource couldn't be found."),
			@com.ibm.commerce.rest.javadoc.ResponseCode(code = 500, reason = "Internal server error. Additional details will be contained on the server logs.") })
	public Response orderItemsAdjustment(@PathParam("storeId") String storeId,
			@QueryParam(value = "responseFormat") String responseFormat) throws Exception {

		String METHODNAME = "orderItemsAdjustment";
		Response response = null;
		try {
			TypedProperty requestProperties = initializeRequestPropertiesFromRequestMap(responseFormat);

			if (responseFormat == null)
				responseFormat = "application/json";

			response = executeControllerCommandWithContext(storeId, CSR_ORDER_ITEMS_ADJUSTMENT_CMD, requestProperties,
					responseFormat);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}

	/**
	 * This method call CSROrderAdjustmentCmd to apply CSR order level adjustment.
	 * 
	 * @param storeId
	 * @param responseFormat
	 * @return
	 * @throws Exception
	 */
	@Path(CSR_ORDERS)
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_XHTML_XML,
			MediaType.APPLICATION_ATOM_XML })
	@ResponseSchema(parameterGroup = RESOURCE_NAME, responseCodes = {
			@com.ibm.commerce.rest.javadoc.ResponseCode(code = 200, reason = "The requested completed successfully."),
			@com.ibm.commerce.rest.javadoc.ResponseCode(code = 400, reason = "Bad request. Some of the inputs provided to the request aren't valid."),
			@com.ibm.commerce.rest.javadoc.ResponseCode(code = 401, reason = "Not authenticated. The user session isn't valid."),
			@com.ibm.commerce.rest.javadoc.ResponseCode(code = 403, reason = "The user isn't authorized to perform the specified request."),
			@com.ibm.commerce.rest.javadoc.ResponseCode(code = 404, reason = "The specified resource couldn't be found."),
			@com.ibm.commerce.rest.javadoc.ResponseCode(code = 500, reason = "Internal server error. Additional details will be contained on the server logs.") })
	public Response ordersAdjustment(@PathParam("storeId") String storeId,
			@QueryParam(value = "responseFormat") String responseFormat) throws Exception {

		String METHODNAME = "ordersAdjustment";
		Response response = null;
		try {
			TypedProperty requestProperties = initializeRequestPropertiesFromRequestMap(responseFormat);

			if (responseFormat == null)
				responseFormat = "application/json";

			response = executeControllerCommandWithContext(storeId, CSR_ORDERS_ADJUSTMENT_CMD, requestProperties,
					responseFormat);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}

	/**
	 * This method call CSRShippingAdjustmentCmd to apply shipping adjustment on order.
	 * 
	 * @param storeId
	 * @param responseFormat
	 * @return
	 * @throws Exception
	 */
	@Path(CSR_ORDER_SHIPPING)
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_XHTML_XML,
			MediaType.APPLICATION_ATOM_XML })
	@ResponseSchema(parameterGroup = RESOURCE_NAME, responseCodes = {
			@com.ibm.commerce.rest.javadoc.ResponseCode(code = 200, reason = "The requested completed successfully."),
			@com.ibm.commerce.rest.javadoc.ResponseCode(code = 400, reason = "Bad request. Some of the inputs provided to the request aren't valid."),
			@com.ibm.commerce.rest.javadoc.ResponseCode(code = 401, reason = "Not authenticated. The user session isn't valid."),
			@com.ibm.commerce.rest.javadoc.ResponseCode(code = 403, reason = "The user isn't authorized to perform the specified request."),
			@com.ibm.commerce.rest.javadoc.ResponseCode(code = 404, reason = "The specified resource couldn't be found."),
			@com.ibm.commerce.rest.javadoc.ResponseCode(code = 500, reason = "Internal server error. Additional details will be contained on the server logs.") })
	public Response orderShippingAdjustment(@PathParam("storeId") String storeId,
			@QueryParam(value = "responseFormat") String responseFormat) throws Exception {

		String METHODNAME = "orderShippingAdjustment";
		Response response = null;
		try {
			TypedProperty requestProperties = initializeRequestPropertiesFromRequestMap(responseFormat);

			if (responseFormat == null)
				responseFormat = "application/json";

			response = executeControllerCommandWithContext(storeId, CSR_ORDERS_SHIPPING_ADJUSTMENT_CMD,
					requestProperties, responseFormat);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}

}
