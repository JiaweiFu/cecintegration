/**
 *
 */
package com.sap.outbound;

import de.hybris.platform.commercefacades.storesession.impl.DefaultStoreSessionFacade;
import de.hybris.platform.commerceservices.model.process.StoreFrontCustomerProcessModel;
import de.hybris.platform.core.Registry;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.task.RetryLaterException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * @author I062917
 *
 */
public class CustomerPubAction extends AbstractSimpleDecisionAction<BusinessProcessModel>
{

	private CustomerSendService sendCustomerToDataHub;

	private DefaultStoreSessionFacade storeSessionFacade;

	/**
	 * @return businessProcessService
	 */
	public BusinessProcessService getBusinessProcessService()
	{
		return (BusinessProcessService) Registry.getApplicationContext().getBean("businessProcessService");
	}

	/**
	 * /** action method to the update the customer and trigger the publish to Data Hub
	 */
	@Override
	public Transition executeAction(final BusinessProcessModel businessProcessModel) throws RetryLaterException
	{
		// set the time stamp in the sap replication info field
		final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		((StoreFrontCustomerProcessModel) businessProcessModel).getCustomer().setSapReplicationInfo(
				"Sent to datahub " + dateFormat.format(Calendar.getInstance().getTime()));

		modelService.save(((StoreFrontCustomerProcessModel) businessProcessModel).getCustomer());

		final BaseStoreModel store = ((StoreFrontCustomerProcessModel) businessProcessModel).getStore();

		// prepare sending data to Data Hub
		final String baseStoreUid = store != null ? store.getUid() : null;
		getSendCustomerToDataHub().sendCustomerData(((StoreFrontCustomerProcessModel) businessProcessModel).getCustomer(),
				baseStoreUid, getStoreSessionFacade().getCurrentLanguage().getIsocode());
		return Transition.OK;
	}

	public CustomerSendService getSendCustomerToDataHub()
	{
		return sendCustomerToDataHub;
	}


	public void setSendCustomerToDataHub(final CustomerSendService sendCustomerToDataHub)
	{
		this.sendCustomerToDataHub = sendCustomerToDataHub;
	}

	public DefaultStoreSessionFacade getStoreSessionFacade()
	{
		return storeSessionFacade;
	}

	public void setStoreSessionFacade(final DefaultStoreSessionFacade storeSessionFacade)
	{
		this.storeSessionFacade = storeSessionFacade;
	}



}
