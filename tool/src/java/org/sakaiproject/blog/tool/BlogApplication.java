package org.sakaiproject.blog.tool;

import org.apache.log4j.Logger;
import org.apache.wicket.Application;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blog.api.BlogManager;
import org.sakaiproject.blog.api.PersistenceManager;
import org.sakaiproject.blog.api.BlogSecurityManager;
import org.sakaiproject.blog.tool.pages.Dispatcher;
import org.sakaiproject.wicket.protocol.http.SakaiWebApplication;

public class BlogApplication extends SakaiWebApplication
{
	private transient Logger logger = Logger.getLogger(BlogApplication.class);

	private BlogManager blogManager;

	protected void init()
	{
		super.init();
		
		if(logger.isDebugEnabled()) logger.debug("init()");
		
		getMarkupSettings().setStripWicketTags(true);

		mountBookmarkablePage("/home", Dispatcher.class);
	}
	
	protected void onDestroy()
	{
		if(logger.isDebugEnabled()) logger.debug("onDestroy()");
	}

	public static BlogApplication get()
	{
		return (BlogApplication) Application.get();
	}

	public Class getHomePage()
	{
		return Dispatcher.class;
	}

	public SakaiProxy getSakaiProxy()
	{
		return blogManager.getSakaiProxy();
	}

	public BlogSecurityManager getSecurityManager()
	{
		return blogManager.getSecurityManager();
	}
	

	public PersistenceManager getPersistenceManager()
	{
		return blogManager.getPersistenceManager();
	}

    public BlogManager getBlogManager()
    {
        return blogManager;
    }

    public void setBlogManager(BlogManager blogManager)
    {
        this.blogManager = blogManager;
    }
}
