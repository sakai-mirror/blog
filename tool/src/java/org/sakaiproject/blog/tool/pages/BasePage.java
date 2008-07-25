package org.sakaiproject.blog.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.blog.tool.BlogApplication;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blog.api.BlogSecurityManager;
import org.sakaiproject.blog.api.PersistenceManager;
import org.sakaiproject.blog.api.BlogManager;
import org.sakaiproject.wicket.markup.html.SakaiPortletWebPage;

public class BasePage extends SakaiPortletWebPage
{
	protected transient BlogManager blogManager;
	protected transient SakaiProxy sakaiProxy;
	protected transient BlogSecurityManager securityManager;
	protected transient PersistenceManager persistenceManager;
	
	private transient Logger logger = Logger.getLogger(BasePage.class);
	protected Link viewAllLink;
	protected Link viewMembersLink;
	protected Link myBlogLink;
	protected Link newPostLink;
	protected Link permissionsPageLink;
	protected Link optionsPageLink;
	protected Link viewRecycledLink;
	protected Link searchLink;
	protected Link preferencesLink;
	
	public BasePage()
	{
		super();
		
		if(logger.isDebugEnabled()) logger.debug("BasePage()");
		
		securityManager = BlogApplication.get().getSecurityManager();
		blogManager = BlogApplication.get().getBlogManager();
		sakaiProxy = BlogApplication.get().getSakaiProxy();
		persistenceManager = BlogApplication.get().getPersistenceManager();
		
		viewAllLink = new Link("viewAllLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new ViewAll());
			}
		};
		viewAllLink.add(new Label("viewAllLabel",new ResourceModel("home")));
		add(viewAllLink);
		
		if(sakaiProxy.isOnGateway())
			viewAllLink.setVisible(false);
		
		viewMembersLink = new Link("viewMembersLink")
		{
			@Override
			public void onClick()
			{
				if(sakaiProxy.isOnGateway())
					setResponsePage(new ViewPublicBloggers());
				else
					setResponsePage(new ViewMembers());
			}
		};
		viewMembersLink.add(new Label("viewMembersLabel",new ResourceModel("viewMembersBlog")));
		add(viewMembersLink);
		
		myBlogLink = new Link("myBlogLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new MemberBlog(BlogApplication.get().getSakaiProxy().getCurrentUserId()));
			}
		};
		myBlogLink.add(new Label("viewMyBlogLabel",new ResourceModel("viewMyBlog")));
		add(myBlogLink);
		
		if(sakaiProxy.isOnGateway())
			myBlogLink.setVisible(false);
		
		newPostLink = new Link("newPostLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new EditPost());
			}
		};
		newPostLink.add(new Label("newPostLabel",new ResourceModel("new")));
		add(newPostLink);
		
		if(!securityManager.canCurrentUserCreatePosts())
			newPostLink.setVisible(false);
		
		permissionsPageLink = new Link("permissionsLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new PermissionsPage());
			}
		};
		
		permissionsPageLink.add(new Label("permissionsLabel",new ResourceModel("permissions")));
		add(permissionsPageLink);
		
		if(!sakaiProxy.isCurrentUserMaintainer())
			permissionsPageLink.setVisible(false);
		
		optionsPageLink = new Link("optionsLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new OptionsPage());
			}
		};
		
		optionsPageLink.add(new Label("optionsLabel",new ResourceModel("options")));
		add(optionsPageLink);
		
		if(!sakaiProxy.isCurrentUserMaintainer())
			optionsPageLink.setVisible(false);
		
		preferencesLink = new Link("preferencesLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new PreferencesPage());
			}
		};
		
		preferencesLink.add(new Label("preferencesLabel",new ResourceModel("preferences")));
		
		add(preferencesLink);
		
		if(sakaiProxy.isOnGateway())
			preferencesLink.setVisible(false);
		
		searchLink = new Link("searchLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new SearchPage());
			}
		};
		
		searchLink.add(new Label("searchLabel",new ResourceModel("search")));
		add(searchLink);
		
		viewRecycledLink = new Link("recycleBinLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new RecycledPage());
			}
		};
		
		viewRecycledLink.add(new Label("recycleBinLabel",new ResourceModel("recycleBin")));
		add(viewRecycledLink);
		
		if(!sakaiProxy.isCurrentUserMaintainer())
			viewRecycledLink.setVisible(false);

		if(persistenceManager.getOptions().isLearningLogMode())
		{
			viewAllLink.setVisible(false);
			myBlogLink.setVisible(false);
			viewMembersLink.setVisible(false);
			
			if(!securityManager.canCurrentUserSearch())
				searchLink.setVisible(false);
			
			if(sakaiProxy.isCurrentUserMaintainer())
				newPostLink.setVisible(false);
		}
	}
}
