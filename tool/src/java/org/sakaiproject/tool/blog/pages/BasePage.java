package org.sakaiproject.tool.blog.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.tool.blog.BlogApplication;
import org.sakaiproject.tool.blog.api.SakaiProxy;
import org.sakaiproject.tool.blog.impl.managers.BlogSecurityManager;
import org.sakaiproject.tool.blog.impl.managers.PersistenceManager;
import org.sakaiproject.tool.blog.impl.managers.PostManager;
import org.sakaiproject.tool.blog.pages.models.PostModel;
import org.sakaiproject.wicket.markup.html.SakaiPortletWebPage;

public class BasePage extends SakaiPortletWebPage
{
	protected transient PostManager postManager;
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
	
	public BasePage()
	{
		super();
		
		if(logger.isDebugEnabled()) logger.debug("BasePage()");
		
		securityManager = BlogApplication.get().getSecurityManager();
		postManager = BlogApplication.get().getPostManager();
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
		
		viewMembersLink = new Link("viewMembersLink")
		{
			@Override
			public void onClick()
			{
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
		
		PersistenceManager pm = BlogApplication.get().getPersistenceManager();
		BlogSecurityManager sm = BlogApplication.get().getSecurityManager();
		
		if(pm.getOptions().isLearningLogMode())
		{
			viewAllLink.setVisible(false);
			myBlogLink.setVisible(false);
			viewMembersLink.setVisible(false);
			
			if(!sm.canCurrentUserSearch())
				searchLink.setVisible(false);
			
			if(sakaiProxy.isCurrentUserMaintainer())
				newPostLink.setVisible(false);
		}
	}
}
