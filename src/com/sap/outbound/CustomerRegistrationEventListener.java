/**
 *
 */
package com.sap.outbound;

import de.hybris.platform.commerceservices.event.RegisterEvent;
import de.hybris.platform.commerceservices.model.process.StoreFrontCustomerProcessModel;
import de.hybris.platform.core.Registry;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;

import org.apache.log4j.Logger;


/**
 * @author I062917
 *
 */
public class CustomerRegistrationEventListener extends AbstractEventListener<RegisterEvent>
{
	private static final Logger LOGGER = Logger
			.getLogger(com.sap.outbound.CustomerRegistrationEventListener.class.getName());

	private ModelService modelService;

	private BaseStoreService baseStoreService;

	/**
	 * @return businessProcessService
	 */
	public BusinessProcessService getBusinessProcessService()
	{
		return (BusinessProcessService) Registry.getApplicationContext().getBean("businessProcessService");
	}

	/**
	 * @return modelService
	 */
	protected ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * start the <code>sapCustomerPublishProcess</code> business process
	 *
	 */
	@Override
	protected void onEvent(final RegisterEvent registerEvent)
	{
		// only if replication of user is requested start publishing to Data Hub process


		final StoreFrontCustomerProcessModel storeFrontCustomerProcessModel = createProcess();
		storeFrontCustomerProcessModel.setSite(registerEvent.getSite());
		storeFrontCustomerProcessModel.setCustomer(registerEvent.getCustomer());

		final BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();
		if (currentBaseStore != null)
		{
			storeFrontCustomerProcessModel.setStore(currentBaseStore);
		}

		getModelService().save(storeFrontCustomerProcessModel);
		getBusinessProcessService().startProcess(storeFrontCustomerProcessModel);

		if (LOGGER.isDebugEnabled())
		{
			if (registerEvent.getCustomer() != null)
			{
				LOGGER.debug("During registration the customer " + registerEvent.getCustomer().getPk()
						+ " was not send to Data Hub. replicate register user not active");
			}
			else
			{
				LOGGER.debug("During registration no customer was send to Data Hub. replicate register user not active");
			}
		}
	}

	/**
	 * Create BusinessProcessService
	 *
	 * @return StoreFrontCustomerProcessModel
	 */
	protected StoreFrontCustomerProcessModel createProcess()
	{
		return (StoreFrontCustomerProcessModel) getBusinessProcessService().createProcess(
				"customerPublishProcess" + System.currentTimeMillis(), "customerPublishProcess");
	}

	/**
	 * return Base store service instance
	 *
	 * @return baseStoreService
	 */
	public BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	/**
	 * Set Base Store Service instance
	 *
	 * @param baseStoreService
	 */
	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}


}
