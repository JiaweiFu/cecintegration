/**
 *
 */
package com.sap.outbound;


import de.hybris.platform.commercefacades.storesession.impl.DefaultStoreSessionFacade;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import de.hybris.platform.store.services.BaseStoreService;

import org.apache.log4j.Logger;


/**
 * @author I062917
 *
 */
/**
 * If default shipment address was updated send default shipment address to Data Hub in case of user replication is
 * active and the address is related to a sap consumer. This is indicated by the filled sap contact id.
 */
public class DefaultCustomerInterceptor implements ValidateInterceptor<CustomerModel>
{

	private static final Logger LOGGER = Logger.getLogger(com.sap.outbound.DefaultCustomerInterceptor.class
			.getName());

	private DefaultStoreSessionFacade storeSessionFacade;
	private CustomerSendService customerExportService;
	private BaseStoreService baseStoreService;


	@Override
	public void onValidate(final CustomerModel customerModel, final InterceptorContext ctx) throws InterceptorException
	{
		// only if replication of user is requested start publishing to Data Hub process

		// check if default shipment address, name or title was updated and the sap contact id is filled
		if ((ctx.isModified(customerModel, CustomerModel.DEFAULTSHIPMENTADDRESS)
				|| ctx.isModified(customerModel, CustomerModel.NAME) || ctx.isModified(customerModel, CustomerModel.TITLE))
			)
		{
			final String baseStoreUid = baseStoreService.getCurrentBaseStore() != null ? baseStoreService.getCurrentBaseStore()
					.getUid() : null;
			final String sessionLanguage = getStoreSessionFacade().getCurrentLanguage() != null ? getStoreSessionFacade()
					.getCurrentLanguage().getIsocode() : null;
			getCustomerExportService().sendCustomerData(customerModel, baseStoreUid, sessionLanguage,
					customerModel.getDefaultShipmentAddress());
		}
		else if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("Customer " + customerModel.getUid() + " was not send to Data Hub.");
			LOGGER.debug("Customer Default shipment address modified =  "
					+ ctx.isModified(customerModel, CustomerModel.DEFAULTSHIPMENTADDRESS));
			LOGGER.debug("Customer name modified = " + ctx.isModified(customerModel, CustomerModel.NAME));
			LOGGER.debug("Customer title modified = " + ctx.isModified(customerModel, CustomerModel.TITLE));
			LOGGER.debug("Customer sapContactId =  " + customerModel.getSapContactID());
		}

		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("Customer " + customerModel.getPk() + " was not send to Data Hub. replicate register user not active");
		}
	}

	/**
	 * @return storeSessionFacade
	 */
	public DefaultStoreSessionFacade getStoreSessionFacade()
	{
		return storeSessionFacade;
	}

	/**
	 * set storeSessionFacade
	 *
	 * @param storeSessionFacade
	 */
	public void setStoreSessionFacade(final DefaultStoreSessionFacade storeSessionFacade)
	{
		this.storeSessionFacade = storeSessionFacade;
	}

	/**
	 * @return customerExportService
	 */
	public CustomerSendService getCustomerExportService()
	{
		return customerExportService;
	}

	/**
	 * set customerExportService
	 *
	 * @param customerExportService
	 */
	public void setCustomerExportService(final CustomerSendService customerExportService)
	{
		this.customerExportService = customerExportService;
	}

	/**
	 * @return baseStoreService
	 */
	public BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	/**
	 * set baseStoreService
	 *
	 * @param baseStoreService
	 */
	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}


}
