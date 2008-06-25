package org.sakaiproject.tool.blog;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.apache.wicket.Application;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.tool.blog.api.BlogFunctions;
import org.sakaiproject.tool.blog.api.SakaiProxy;
import org.sakaiproject.tool.blog.entity.BlogEntityProducer;
import org.sakaiproject.tool.blog.impl.managers.BlogSecurityManager;
import org.sakaiproject.tool.blog.impl.managers.PostManager;
import org.sakaiproject.tool.blog.impl.managers.PersistenceManager;
import org.sakaiproject.tool.blog.pages.Dispatcher;
import org.sakaiproject.wicket.protocol.http.SakaiWebApplication;

public class BlogApplication extends SakaiWebApplication implements Observer
{
	private transient Logger logger = Logger.getLogger(BlogApplication.class);

	private PostManager postManager = null;

	private SakaiProxy sakaiProxy;

	private BlogSecurityManager securityManager;

	private PersistenceManager persistenceManager;

	private BlogEntityProducer blogEntityProducer;

	protected void init()
	{
		super.init();
		
		if(logger.isDebugEnabled()) logger.debug("init()");
		
		getMarkupSettings().setStripWicketTags(true);

		try
		{
			persistenceManager = new PersistenceManager();
			persistenceManager.setSakaiProxy(sakaiProxy);
			persistenceManager.init();

			securityManager = new BlogSecurityManager();
			securityManager.setSakaiProxy(sakaiProxy);
			securityManager.setPersistenceManager(persistenceManager);

			postManager = new PostManager();
			postManager.setPersistenceManager(persistenceManager);
			postManager.setSecurityManager(securityManager);
			
			logger.info("Registering Blog functions ...");
			
			sakaiProxy.registerFunction(BlogFunctions.BLOG_POST_CREATE);
			sakaiProxy.registerFunction(BlogFunctions.BLOG_POST_READ_ANY);
			sakaiProxy.registerFunction(BlogFunctions.BLOG_POST_READ_OWN);
			sakaiProxy.registerFunction(BlogFunctions.BLOG_POST_UPDATE_ANY);
			sakaiProxy.registerFunction(BlogFunctions.BLOG_POST_UPDATE_OWN);
			sakaiProxy.registerFunction(BlogFunctions.BLOG_POST_DELETE_ANY);
			sakaiProxy.registerFunction(BlogFunctions.BLOG_POST_DELETE_OWN);
			sakaiProxy.registerFunction(BlogFunctions.BLOG_COMMENT_CREATE);
			sakaiProxy.registerFunction(BlogFunctions.BLOG_COMMENT_READ_ANY);
			sakaiProxy.registerFunction(BlogFunctions.BLOG_COMMENT_READ_OWN);
			sakaiProxy.registerFunction(BlogFunctions.BLOG_COMMENT_UPDATE_ANY);
			sakaiProxy.registerFunction(BlogFunctions.BLOG_COMMENT_UPDATE_OWN);
			sakaiProxy.registerFunction(BlogFunctions.BLOG_COMMENT_DELETE_ANY);
			sakaiProxy.registerFunction(BlogFunctions.BLOG_COMMENT_DELETE_OWN);
			
			logger.info("Registered Blog functions ...");
			
			sakaiProxy.addEventObserver(this);
			
			blogEntityProducer = new BlogEntityProducer();
			blogEntityProducer.setSakaiProxy(sakaiProxy);
			blogEntityProducer.setPostManager(postManager);
			
			sakaiProxy.registerEntityProducer(blogEntityProducer);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mountBookmarkablePage("/home", Dispatcher.class);
	}
	
	protected void onDestroy()
	{
		if(logger.isDebugEnabled()) logger.debug("onDestroy()");
		
		sakaiProxy.deleteEventObserver(this);
	}

	public static BlogApplication get()
	{
		return (BlogApplication) Application.get();
	}

	public Class getHomePage()
	{
		return Dispatcher.class;
	}

	public void setSakaiProxy(SakaiProxy sakaiProxy)
	{
		this.sakaiProxy = sakaiProxy;
	}

	public SakaiProxy getSakaiProxy()
	{
		return sakaiProxy;
	}

	public BlogSecurityManager getSecurityManager()
	{
		return securityManager;
	}
	
	public PostManager getPostManager()
	{
		return postManager;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public void update(Observable o, Object arg)
	{
		Event e = (Event) arg;
		String event = e.getEvent();
		String resourceId = e.getResource();
		
		logger.debug("Event: " + event + ". Resource: " + resourceId);
		
		if(event.equals(ContentHostingService.EVENT_RESOURCE_REMOVE))
		{
			//postManager.removeResourceReference(resourceId);
		}
	}
}
