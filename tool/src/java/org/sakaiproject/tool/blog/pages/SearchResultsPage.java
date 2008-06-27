package org.sakaiproject.tool.blog.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.tool.blog.api.QueryBean;

public class SearchResultsPage extends BasePage
{
	private transient Logger logger = Logger.getLogger(SearchResultsPage.class);
	
	public SearchResultsPage()
	{
		this(null,5,0);
		
		if(logger.isDebugEnabled()) logger.debug("SearchResultsPage()");
	}
	
	public SearchResultsPage(QueryBean q)
	{
		this(q,5,0);
	}
	
	public SearchResultsPage(final QueryBean query,int pageSize,int currentPage)
	{
		super();
		
		if(logger.isDebugEnabled()) logger.debug("SearchResultsPage(" + pageSize + "," + currentPage + ")");
		
		add(new Label("searchResultsLabel",new ResourceModel("searchResults")));
		
		Link backToSearchLink = new Link("backToSearchLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new SearchPage(query));
			}
		};
		
		backToSearchLink.add(new Label("backToSearchLabel",new ResourceModel("backToSearch")));
		add(backToSearchLink);
		
		add(new PostsPanel("postsPanel",query));
	}
}
