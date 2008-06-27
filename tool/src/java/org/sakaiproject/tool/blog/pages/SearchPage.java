package org.sakaiproject.tool.blog.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.tool.blog.api.QueryBean;
import org.sakaiproject.tool.blog.api.datamodel.State;

public class SearchPage extends BasePage
{
	private transient Logger logger = Logger.getLogger(SearchPage.class);
	private QueryBean query;
	
	public SearchPage()
	{
		this(null,5,0);
		
		if(logger.isDebugEnabled()) logger.debug("SearchPage()");
	}
	
	public SearchPage(QueryBean q)
	{
		this(q,5,0);
	}
	
	public SearchPage(QueryBean q,int pageSize,int currentPage)
	{
		super();
		
		query = q;
		
		if(query == null)
			query = new QueryBean();
		
		if(logger.isDebugEnabled()) logger.debug("SearchPage(" + pageSize + "," + currentPage + ")");
		
		add(new Label("searchLabel",new ResourceModel("search")));
		
		Form searchForm = new Form("searchForm")
		{
			protected void onSubmit()
			{
				if(query != null)
				{
					if(sakaiProxy.isOnGateway())
						query.setVisibilities(new String[] {State.PUBLIC});
					else
						query.setSiteId(sakaiProxy.getCurrentSiteId());
				}
				
				setResponsePage(new SearchResultsPage(query));
			}
		};
		
		searchForm.add(new TextField("searchField",new PropertyModel(query,"queryString")));
		searchForm.add(new Button("searchButton",new ResourceModel("search")));
		
		add(searchForm);
	}
}
