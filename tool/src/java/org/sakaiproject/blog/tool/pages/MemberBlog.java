package org.sakaiproject.blog.tool.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.model.Model;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.blog.api.QueryBean;

public class MemberBlog extends BasePage
{
	private static final String UNAVAILABLE_IMAGE = "img/no_image.gif";
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
