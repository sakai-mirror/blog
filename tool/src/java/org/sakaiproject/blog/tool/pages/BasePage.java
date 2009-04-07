package org.sakaiproject.blog.tool.pages;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;
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
	private String message = "";
	private Label messageLabel;
	
	public BasePage()
	{
		super();
		
		if(logger.isDebugEnabled()) logger.debug("BasePage()");
		
		securityManager = BlogApplication.get().getSecurityManager();
		blogManager = BlogApplication.get().getBlogManager();
		sakaiProxy = BlogApplication.get().getSakaiProxy();
		persistenceManager = BlogApplication.get().getPersistenceManager();
		
		//set Locale - all pages will inherit this.
		setUserPreferredLocale();
		
		
		viewAllLink = new Link("viewAllLink")
		{
			@Override
			public void onClick()
			{
				if(persistenceManager.getOptions().isLearningLogMode())
				{
					if(sakaiProxy.isCurrentUserMaintainer() || sakaiProxy.isCurrentUserTutor())
						setResponsePage(new ViewMembers());
					else
						setResponsePage(new MemberBlog());
				}
				else
					setResponsePage(new ViewAll());
			}
		};
		viewAllLink.add(new Label("viewAllLabel",new ResourceModel("home")));
		viewAllLink.add(new AttributeModifier("title",true,new ResourceModel("homeTooltip")));
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
		viewMembersLink.add(new AttributeModifier("title",true,new ResourceModel("viewMembersBlogTooltip")));
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
		myBlogLink.add(new AttributeModifier("title",true,new ResourceModel("viewMyBlogTooltip")));
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
		newPostLink.add(new AttributeModifier("title",true,new ResourceModel("newTooltip")));
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
		permissionsPageLink.add(new AttributeModifier("title",true,new ResourceModel("permissionsTooltip")));
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
		optionsPageLink.add(new AttributeModifier("title",true,new ResourceModel("optionsTooltip")));
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
		preferencesLink.add(new AttributeModifier("title",true,new ResourceModel("preferencesTooltip")));
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
		searchLink.add(new AttributeModifier("title",true,new ResourceModel("searchTooltip")));
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
		viewRecycledLink.add(new AttributeModifier("title",true,new ResourceModel("recycleBinTooltip")));
		add(viewRecycledLink);
		
		if(!sakaiProxy.isCurrentUserMaintainer())
			viewRecycledLink.setVisible(false);

		if(persistenceManager.getOptions().isLearningLogMode())
		{
			//viewAllLink.setVisible(false);
			myBlogLink.setVisible(false);
			preferencesLink.setVisible(false);
			viewMembersLink.setVisible(false);
			
			if(!securityManager.canCurrentUserSearch())
				searchLink.setVisible(false);
			
			if(sakaiProxy.isCurrentUserMaintainer())
				newPostLink.setVisible(false);
		}
		
		messageLabel = new Label("messageLabel",new PropertyModel(this,"message"));
		messageLabel.setVisible(false);
		add(messageLabel);
	}

	public void setMessage(String message)
	{
		this.message = message;
		messageLabel.setVisible(true);
	}

	public String getMessage()
	{
		return message;
	}
	
	public void setUserPreferredLocale() {
		Locale sakaiLocale = sakaiProxy.getUserPreferredLocale();
		logger.debug("User preferred locale: " + sakaiLocale);
		getSession().setLocale(sakaiLocale);
	}
}
