package org.sakaiproject.tool.blog.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;

public class ViewAll extends BasePage
{
	private transient Logger logger = Logger.getLogger(ViewAll.class);
	
	public ViewAll()
	{
		this(5,0);
		
		if(logger.isDebugEnabled()) logger.debug("ViewAll()");
	}
	
	public ViewAll(int pageSize,int currentPage)
	{
		super();
		
		if(logger.isDebugEnabled()) logger.debug("ViewAll(" + pageSize + "," + currentPage + ")");
		
		viewAllLink.setVisible(false);
		
		add(new Label("home",new ResourceModel("home")));
		
		add(new PostsPanel("postsPanel"));
	}
}
