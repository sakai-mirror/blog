package org.sakaiproject.blog.tool;

import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.wicket.Application;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.sakaiproject.blog.api.BlogManager;
import org.sakaiproject.blog.api.BlogSecurityManager;
import org.sakaiproject.blog.api.PersistenceManager;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blog.tool.pages.Dispatcher;

public class BlogApplication extends WebApplication
{
	private transient Logger logger = Logger.getLogger(BlogApplication.class);

	private BlogManager blogManager;

	protected void init()
	{
		super.init();
		
		if(logger.isDebugEnabled()) logger.debug("init()");
		
		getMarkupSettings().setStripWicketTags(true);
		addComponentInstantiationListener(new SpringComponentInjector(this));
		getResourceSettings().setThrowExceptionOnMissingResource(true);

		//mountBookmarkablePage("/home", Dispatcher.class);
	}
	
	protected void onDestroy()
	{
		if(logger.isDebugEnabled()) logger.debug("onDestroy()");
	}

	public static BlogApplication get()
	{
		return (BlogApplication) Application.get();
	}
	
	public String getConfigurationType() { return Application.DEVELOPMENT; }

	public Class getHomePage()
	{
		return Dispatcher.class;
	}
	
	public RequestCycle newRequestCycle(Request request,Response response)
	{
		logger.debug("URL: " + request.getURL());
		
		Map params = request.getParameterMap();
		
		for(Object key : params.keySet())
		{
			logger.debug(key + ":" + params.get(key));
		}
		
		return super.newRequestCycle(request, response);
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
