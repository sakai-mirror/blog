package org.sakaiproject.blogadministrator.tool;

import org.apache.log4j.Logger;
import org.apache.wicket.Application;
import org.sakaiproject.wicket.protocol.http.SakaiWebApplication;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blog.api.BlogManager;
import org.sakaiproject.blogadministrator.tool.pages.Main;

public class BlogDataMigratorApplication extends SakaiWebApplication
{
	private transient Logger logger = Logger.getLogger(BlogDataMigratorApplication.class);

	private BlogManager blogManager;

	protected void init()
	{
		super.init();
		
		if(logger.isDebugEnabled()) logger.debug("init()");
		
		getMarkupSettings().setStripWicketTags(true);

		mountBookmarkablePage("/home", Main.class);
	}
	
	protected void onDestroy()
	{
		if(logger.isDebugEnabled()) logger.debug("onDestroy()");
	}

	public static BlogDataMigratorApplication get()
	{
		return (BlogDataMigratorApplication) Application.get();
	}

	public Class getHomePage()
	{
		return Main.class;
	}

	public SakaiProxy getSakaiProxy()
	{
		return blogManager.getSakaiProxy();
	}

	public void setBlogManager(BlogManager postManager)
	{
		this.blogManager = postManager;
	}

	public BlogManager getBlogManager()
	{
		return blogManager;
	}
}
