package org.sakaiproject.blog.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.blog.tool.BlogApplication;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blog.api.BlogSecurityManager;
import org.sakaiproject.blog.api.PersistenceManager;
import org.sakaiproject.blog.api.BlogManager;

public class BasePage extends WebPage implements IHeaderContributor
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
	protected Link viewRecycledLink;
	protected Link searchLink;
	protected Link preferencesLink;
	private String message = "";
	private Label messageLabel;
	
	public BasePage()
	{
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
		viewAllLink.add(new AttributeModifier("title",true,new ResourceModel("homeTooltip")));
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
	
	//Style it like a Sakai tool
    protected static final String HEADSCRIPTS = "/library/js/headscripts.js";
    protected static final String BODY_ONLOAD_ADDTL="setMainFrameHeight( window.name )";

	public void renderHead(IHeaderResponse response)
	{
		//get Sakai skin
        String skinRepo = sakaiProxy.getSakaiProperty("skin.repo");
        String toolCSS = getToolSkinCSS(skinRepo);
        String toolBaseCSS = skinRepo + "/tool_base.css";
   
        //Sakai additions
        response.renderJavascriptReference(HEADSCRIPTS);
        response.renderCSSReference(toolBaseCSS);
        response.renderCSSReference(toolCSS);
        response.renderOnLoadJavascript(BODY_ONLOAD_ADDTL);
	}
	
	protected String getToolSkinCSS(String skinRepo)
	{
        String skin = null;
        try
        {
            skin = sakaiProxy.getCurrentTool().getSkin();
            //skin = SiteService.findTool(SessionManager.getCurrentToolSession().getPlacementId()).getSkin();
        }
        catch(Exception e)
        {
        	skin = sakaiProxy.getSakaiProperty("skin.default");
        }

        if(skin == null)
        {
            skin = sakaiProxy.getSakaiProperty("skin.default");
        }

        return skinRepo + "/" + skin + "/tool.css";
    }
}
