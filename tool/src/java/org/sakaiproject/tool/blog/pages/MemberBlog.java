package org.sakaiproject.tool.blog.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.model.Model;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.tool.blog.BlogApplication;
import org.sakaiproject.tool.blog.api.BlogFunctions;
import org.sakaiproject.tool.blog.api.QueryBean;
import org.sakaiproject.tool.blog.impl.managers.PersistenceManager;
//import org.wicketstuff.rome.FeedResource;

public class MemberBlog extends BasePage
{
	private static final String UNAVAILABLE_IMAGE = "img/officialPhotoUnavailable.jpg";
	private byte[] pictureBytes;
	
	private String userId;
	
	public MemberBlog()
	{
		this(null);
	}
	
	public MemberBlog(String userId)
	{
		this(5,0,userId);
	}
	
	public MemberBlog(int pageSize,int currentPage,String id)
	{
		super();
		
		this.userId = id;
		
		PersistenceManager pm = BlogApplication.get().getPersistenceManager();
		if(pm.getOptions().isLearningLogMode())
		{
			if(sakaiProxy.isAllowedFunction(BlogFunctions.BLOG_POST_READ_OWN))
			{
				viewAllLink.setVisible(false);
				viewMembersLink.setVisible(false);
				myBlogLink.setVisible(false);
			}
		}
		
		if(userId == null)
			userId = sakaiProxy.getCurrentUserId();
		
		String displayName
			= sakaiProxy.getDisplayNameForTheUser(userId);
		
		add(new Label("displayName",displayName));
		
		String blurb = "";
		
		Profile profile = sakaiProxy.getProfile(userId);
		
		if(profile != null)
		{
			blurb = profile.getOtherInformation();
			pictureBytes = profile.getInstitutionalPicture();
		}
		
		Label blurbLabel = new Label("profileBlurb",blurb);
		blurbLabel.setEscapeModelStrings(false);
		add(blurbLabel);
		
		if(pictureBytes != null && pictureBytes.length > 0)
		{
			BufferedDynamicImageResource photoResource = new BufferedDynamicImageResource()
			{
				protected byte[] getImageData()
				{
					return pictureBytes;
				}
			};
		
			add(new Image("photo",photoResource));
		}
		else
			add(new ContextImage("photo",new Model(UNAVAILABLE_IMAGE)));
		
		//add(new ResourceLink("rssLink",new MemberBlogFeedResource(userId)));
		
		QueryBean query = new QueryBean();
		query.setCreator(userId);
		query.setSiteId(sakaiProxy.getCurrentSiteId());
		add(new PostsPanel("postsPanel",query));
	}
}
