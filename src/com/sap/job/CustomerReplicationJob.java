/**
 *
 */
package com.sap.job;

import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sap.outbound.CustomerSendService;


/**
 * @author I062917
 *
 */
public class CustomerReplicationJob extends AbstractJobPerformable<CronJobModel>
{

	private static final Logger LOG = Logger.getLogger(CustomerReplicationJob.class);

	private CustomerSendService customerSendService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.CronJobModel
	 * )
	 */
	@Override
	public PerformResult perform(final CronJobModel arg0)
	{

		//		final String query = "SELECT {UID},{SESSIONLANGUAGE},{TITLE},{PK} FROM {Customer}";

		final String query = "SELECT {" + CustomerModel.PK + "} FROM { "
				+ CustomerModel._TYPECODE + "}";
		//		final String query = "SELECT {" + CustomerModel.CUSTOMERID + "}, "
		//				+ "{" + CustomerModel.UID + "}, "
		//				+ "{" + CustomerModel.SESSIONLANGUAGE + "}, "
		//				+ "{" + CustomerModel.TITLE + "}, "
		//				+ "{" + CustomerModel.PK + "} FROM { "
		//				+ CustomerModel._TYPECODE + "}";

		final SearchResult<CustomerModel> searchResult = this.flexibleSearchService.search(query, null);
		final List<CustomerModel> customerModel = searchResult.getResult();
		final ArrayList custs = new ArrayList();
		custs.addAll(customerModel);
		LOG.info("The customer model's size" + custs.size());
		if (customerModel.size() == 0)
		{
			LOG.info("No customer to replicate, skip!");
			LOG.info("count ！！！" + searchResult.getCount());
			return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
		}
		for (int i = 0; i < customerModel.size(); i++)
		{
			LOG.info("Replicate Customer" + customerModel.get(i).getCustomerID());
			final CustomerModel cus = customerModel.get(i);
			this.setupAddress(cus);
			this.getCustomerSendService().sendCustomerData_Full(cus);
		}

		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	private void setupAddress(final CustomerModel c_model)
	{

		final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(
				"SELECT {pk} FROM {Address AS a} WHERE  {a.owner} = ?customerPK");
		flexibleSearchQuery.addQueryParameter("customerPK", c_model.getPk());

		final SearchResult<AddressModel> searchResult = this.flexibleSearchService.search(flexibleSearchQuery);
		final List<AddressModel> addressModel = searchResult.getResult();

		if (addressModel.size() == 0)
		{
			LOG.info("No address to replicate, skip!");
		}
		else
		{
			LOG.info("Replicate address");
			c_model.setAddresses(addressModel);
		}

	}

	public CustomerSendService getCustomerSendService()
	{
		return customerSendService;
	}

	public void setCustomerSendService(final CustomerSendService customerSendService)
	{
		this.customerSendService = customerSendService;
	}




}
