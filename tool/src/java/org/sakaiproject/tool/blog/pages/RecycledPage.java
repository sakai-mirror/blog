package org.sakaiproject.tool.blog.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.tool.blog.api.datamodel.State;

public class RecycledPage extends BasePage
{
	private transient Logger logger = Logger.getLogger(RecycledPage.class);
	
	public RecycledPage()
	{
		this(5,0);
		
		if(logger.isDebugEnabled()) logger.debug("RecycledPage()");
	}
	
	public RecycledPage(int pageSize,int currentPage)
	{
		super();
		
		if(logger.isDebugEnabled()) logger.debug("RecycledPage(" + pageSize + "," + currentPage + ")");
		
		viewRecycledLink.setVisible(false);
		
		add(new Label("recycleBinLabel",new ResourceModel("recycleBin")));
		
		add(new RecycledPostsPanel("postsPanel",null));
	}
}
