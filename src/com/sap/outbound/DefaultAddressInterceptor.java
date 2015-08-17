/**
 *
 */
package com.sap.outbound;

import de.hybris.platform.commercefacades.storesession.impl.DefaultStoreSessionFacade;
import de.hybris.platform.core.model.user.AddressModel;
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
public class DefaultAddressInterceptor implements ValidateInterceptor<AddressModel>
{
	private static final Logger LOGGER = Logger.getLogger(com.sap.outbound.DefaultAddressInterceptor.class
			.getName());
	private DefaultStoreSessionFacade storeSessionFacade;
	private CustomerSendService customerExportService;
	private BaseStoreService baseStoreService;


	@Override
	public void onValidate(final AddressModel addressModel, final InterceptorContext ctx) throws InterceptorException
	{
		// only if replication of user is requested start publishing to Data Hub process

		if (addressModel.getOwner() instanceof CustomerModel)
		{
			final CustomerModel customerModel = ((CustomerModel) addressModel.getOwner());
			// check if default shipment address was updated
			if (ctx.isModified(addressModel, AddressModel.COUNTRY) || ctx.isModified(addressModel, AddressModel.STREETNAME)
					|| ctx.isModified(addressModel, AddressModel.PHONE1) || ctx.isModified(addressModel, AddressModel.FAX)
					|| ctx.isModified(addressModel, AddressModel.TOWN) || ctx.isModified(addressModel, AddressModel.POSTALCODE)
					|| ctx.isModified(addressModel, AddressModel.STREETNUMBER) || ctx.isModified(addressModel, AddressModel.REGION)
					)
			{
				if (customerModel.getDefaultShipmentAddress() != null && isDefaultShipmentAddress(addressModel))
				{
					final String baseStoreUid = baseStoreService.getCurrentBaseStore() != null ? baseStoreService
							.getCurrentBaseStore().getUid() : null;
					final String sessionLanguage = getStoreSessionFacade().getCurrentLanguage() != null ? getStoreSessionFacade()
							.getCurrentLanguage().getIsocode() : null;
					getCustomerExportService().sendCustomerData(customerModel, baseStoreUid, sessionLanguage, addressModel);
				}
				else if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("Default shipment address is null or updated address " + addressModel.getPk()
							+ " is not a default shipment address");
				}
			}
			else if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("Address " + addressModel.getPk() + " was not send to Data Hub.");
				LOGGER.debug("Address country modified =  " + ctx.isModified(addressModel, AddressModel.COUNTRY));
				LOGGER.debug("Address streetname modified = " + ctx.isModified(addressModel, AddressModel.STREETNAME));
				LOGGER.debug("Address phone1 modified = " + ctx.isModified(addressModel, AddressModel.PHONE1));
				LOGGER.debug("Address fax modified = " + ctx.isModified(addressModel, AddressModel.FAX));
				LOGGER.debug("Address town modified = " + ctx.isModified(addressModel, AddressModel.TOWN));
				LOGGER.debug("Address postalcode modified = " + ctx.isModified(addressModel, AddressModel.POSTALCODE));
				LOGGER.debug("Address streetnumber modified = " + ctx.isModified(addressModel, AddressModel.STREETNUMBER));
				LOGGER.debug("Address region modified = " + ctx.isModified(addressModel, AddressModel.REGION));
				LOGGER.debug("Customer sapContactId = " + customerModel.getSapContactID());
			}
		}

		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("Address " + addressModel.getPk() + " was not send to Data Hub. replicate register user not active");
		}

	}

	/**
	 * @param addressModel
	 * @return <code>true</code> if the address is also assigned as default shipment address to the address owner
	 */
	protected boolean isDefaultShipmentAddress(final AddressModel addressModel)
	{
		return ((CustomerModel) addressModel.getOwner()).getDefaultShipmentAddress().getPk().equals(addressModel.getPk());

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
