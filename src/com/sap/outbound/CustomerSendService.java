/**
 *
 */
package com.sap.outbound;

import static com.sap.constants.CecintegrationConstants.ADDRESS_ID;
import static com.sap.constants.CecintegrationConstants.COUNTRY;
import static com.sap.constants.CecintegrationConstants.COUNTRY_DE;
import static com.sap.constants.CecintegrationConstants.CUSTOMER_ID;
import static com.sap.constants.CecintegrationConstants.DEFAULT_FEED;
import static com.sap.constants.CecintegrationConstants.FIRSTNAME;
import static com.sap.constants.CecintegrationConstants.LASTNAME;
import static com.sap.constants.CecintegrationConstants.OBJ_TYPE;
import static com.sap.constants.CecintegrationConstants.PHONE;
import static com.sap.constants.CecintegrationConstants.POSTALCODE;
import static com.sap.constants.CecintegrationConstants.RAW_CEC_CUSTOMER;
import static com.sap.constants.CecintegrationConstants.REGION;
import static com.sap.constants.CecintegrationConstants.SESSION_LANGUAGE;
import static com.sap.constants.CecintegrationConstants.STREET;
import static com.sap.constants.CecintegrationConstants.STREETNUMBER;
import static com.sap.constants.CecintegrationConstants.TITLE;
import static com.sap.constants.CecintegrationConstants.TOWN;
import static com.sap.constants.CecintegrationConstants.UID;

import de.hybris.platform.commerceservices.strategies.CustomerNameStrategy;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hybris.datahub.core.rest.DataHubCommunicationException;
import com.hybris.datahub.core.rest.DataHubOutboundException;
import com.hybris.datahub.core.services.DataHubOutboundService;


/**
 * @author I062917
 *
 */
public class CustomerSendService
{
	private static final Logger LOGGER = Logger.getLogger(com.sap.outbound.CustomerSendService.class
			.getName());

	private CustomerNameStrategy customerNameStrategy;
	private DataHubOutboundService dataHubOutboundService;

	private String feed = DEFAULT_FEED;
	private final String country = COUNTRY_DE;

	/**
	 * return Data Hub Outbound Service
	 *
	 * @param dataHubOutboundService
	 */
	public DataHubOutboundService getDataHubOutboundService()
	{
		return dataHubOutboundService;
	}

	/**
	 * set Data Hub Outbound Service
	 *
	 * @param dataHubOutboundService
	 */
	public void setDataHubOutboundService(final DataHubOutboundService dataHubOutboundService)
	{
		this.dataHubOutboundService = dataHubOutboundService;
	}

	public void sendCustomerData_Full(final CustomerModel customerModel)
	{

		final Map<String, Object> target = getTarget();

		if (customerModel.getAddresses().size() > 0)
		{
			final Iterator<AddressModel> it = customerModel.getAddresses().iterator();
			while (it.hasNext())
			{
				final AddressModel address = it.next();
				final String[] names = customerNameStrategy.splitName(customerModel.getName());
				LOGGER.info("CUSTOMER UID" + customerModel.getUid());
				target.put(UID, customerModel.getUid());
				LOGGER.info("CUSTOMER ID" + customerModel.getCustomerID());
				target.put(CUSTOMER_ID, customerModel.getCustomerID());
				target.put(FIRSTNAME, names[0]);
				target.put(LASTNAME, names[1]);
				if (customerModel.getSessionLanguage() != null)
				{
					LOGGER.info("The session language name: " + customerModel.getSessionLanguage().getName());
					LOGGER.info("The session launguage iso code: " + customerModel.getSessionLanguage().getIsocode());
					target.put(SESSION_LANGUAGE, customerModel.getSessionLanguage().getIsocode());
				}
				else
				{
					target.put(SESSION_LANGUAGE, "");
				}
				if (customerModel.getTitle() != null)
				{
					target.put(TITLE, customerModel.getTitle().getCode());
				}
				else
				{
					target.put(TITLE, "");
				}
				final AddressModel addressModel = address;
				prepareAddressData(addressModel, target);

				LOGGER.info("Begin to setnd customer!");
				sendCustomerToDataHub(target);
			}
		}
		else
		{
			final String[] names = customerNameStrategy.splitName(customerModel.getName());
			LOGGER.info("CUSTOMER UID" + customerModel.getUid());
			target.put(UID, customerModel.getUid());
			LOGGER.info("CUSTOMER ID" + customerModel.getCustomerID());
			target.put(CUSTOMER_ID, customerModel.getCustomerID());
			target.put(FIRSTNAME, names[0]);
			target.put(LASTNAME, names[1]);

			if (customerModel.getSessionLanguage() != null)
			{
				LOGGER.info("The session language name: " + customerModel.getSessionLanguage().getName());
				LOGGER.info("The session launguage iso code: " + customerModel.getSessionLanguage().getIsocode());
				target.put(SESSION_LANGUAGE, customerModel.getSessionLanguage().getIsocode());
			}
			else
			{
				target.put(SESSION_LANGUAGE, "");
			}
			if (customerModel.getTitle() != null)
			{
				target.put(TITLE, customerModel.getTitle().getCode());
			}
			else
			{
				target.put(TITLE, "");
			}
			sendCustomerToDataHub(target);
		}

	}

	/**
	 * map customer Model to the target map, set session language and base store name, and send data to the Data Hub
	 *
	 * @param customerModel
	 * @param baseStoreUid
	 * @param sessionLanguage
	 */
	public void sendCustomerData(final CustomerModel customerModel, final String baseStoreUid, final String sessionLanguage)
	{
		sendCustomerData(customerModel, baseStoreUid, sessionLanguage, null);
	}

	/**
	 * map customer Model and address Model to the target map, set session language and base store name, and send data to
	 * the Data Hub
	 *
	 * @param customerModel
	 * @param baseStoreUid
	 * @param sessionLanguage
	 * @param addressModel
	 */
	public void sendCustomerData(final CustomerModel customerModel, final String baseStoreUid, final String sessionLanguage,
			final AddressModel addressModel)
	{
		final Map<String, Object> target = getTarget();

		prepareCustomerData(customerModel, baseStoreUid, sessionLanguage, target);

		if (addressModel == null)
		{
			target.put(COUNTRY, country);
		}
		else
		{
			prepareAddressData(addressModel, target);
		}
		sendCustomerToDataHub(target);
	}

	/**
	 * @return new target instance
	 */
	protected Map<String, Object> getTarget()
	{
		return new HashMap<String, Object>();
	}

	protected void prepareCustomerData(final CustomerModel customerModel, final String baseStoreUid, final String sessionLanguage,
			final Map<String, Object> target)
	{
		final String[] names = customerNameStrategy.splitName(customerModel.getName());

		target.put(UID, customerModel.getUid());
		target.put(CUSTOMER_ID, customerModel.getCustomerID());
		target.put(FIRSTNAME, names[0]);
		target.put(LASTNAME, names[1]);
		if (customerModel.getSessionLanguage() != null)
		{
			LOGGER.info("The session language name: " + customerModel.getSessionLanguage().getName());
			LOGGER.info("The session launguage iso code: " + customerModel.getSessionLanguage().getIsocode());
			target.put(SESSION_LANGUAGE, customerModel.getSessionLanguage().getIsocode());
		}
		else
		{
			target.put(SESSION_LANGUAGE, "");
		}
		if (customerModel.getTitle() != null)
		{
			target.put(TITLE, customerModel.getTitle().getCode());
		}
		else
		{
			target.put(TITLE, "");
		}

		//		target.put(ADDRESS_USAGE, ADDRESSUSAGE_DE);
		//		target.put(BASE_STORE, baseStoreUid);
		//		target.put(OBJ_TYPE, OBJTYPE_KNA1);
		//		target.put(CONTACT_ID, customerModel.getSapContactID());
	}

	protected void prepareAddressData(final AddressModel addressModel, final Map<String, Object> target)
	{
		final String countryIsoCode = addressModel.getCountry() != null ? addressModel.getCountry().getIsocode() : null;
		target.put(COUNTRY, countryIsoCode);

		target.put(ADDRESS_ID, addressModel.getPk().getLongValueAsString());
		target.put(STREET, addressModel.getStreetname());
		target.put(STREETNUMBER, addressModel.getStreetnumber());
		target.put(PHONE, addressModel.getPhone1());
		target.put(TOWN, addressModel.getTown());
		target.put(POSTALCODE, addressModel.getPostalcode());
		final StringBuffer addressType = new StringBuffer();
		if (addressModel.getBillingAddress().booleanValue())
		{
			addressType.append("billing;");
		}
		else if (addressModel.getShippingAddress().booleanValue())
		{
			addressType.append("shipping;");
		}
		target.put(OBJ_TYPE, addressType.toString());

		final String regionIsoCode = addressModel.getRegion() != null ? addressModel.getRegion().getIsocode() : null;
		target.put(REGION, regionIsoCode);

		//		target.put(FAX, addressModel.getFax());
	}

	protected void sendCustomerToDataHub(final Map<String, Object> target)
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("The following values was send to Data Hub" + target);
			LOGGER.debug("To the feed" + getFeed() + " into raw model " + RAW_CEC_CUSTOMER);
		}
		try
		{
			LOGGER.info("Sending out customer !");
			getDataHubOutboundService().sendToDataHub(getFeed(), RAW_CEC_CUSTOMER, target);
		}
		catch (final DataHubOutboundException e)
		{
			LOGGER.warn("Error processing sending data to Data Hub. DataHubOutboundException: " + e.getMessage());
		}
		catch (final DataHubCommunicationException e)
		{
			LOGGER.warn("Error processing sending data to Data Hub. DataHubCommunicationException: " + e.getMessage());
		}
	}

	/**
	 * return data hub feed
	 *
	 * @return feed
	 */
	public String getFeed()
	{
		return feed;
	}


	/**
	 * set data hub feed (usually set via the local property file)
	 *
	 * @param feed
	 */
	public void setFeed(final String feed)
	{
		this.feed = feed;
	}

	/**
	 * @return customerNameStrategy
	 */
	public CustomerNameStrategy getCustomerNameStrategy()
	{
		return customerNameStrategy;
	}

	/**
	 * @param customerNameStrategy
	 */
	public void setCustomerNameStrategy(final CustomerNameStrategy customerNameStrategy)
	{
		this.customerNameStrategy = customerNameStrategy;
	}


}
