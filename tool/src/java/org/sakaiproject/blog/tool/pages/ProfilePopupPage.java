package org.sakaiproject.blog.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.resource.ContextRelativeResource;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blog.tool.BlogApplication;

public class ProfilePopupPage extends WebPage
{
	private transient Logger logger = Logger.getLogger(ProfilePopupPage.class);
	private static final String UNAVAILABLE_IMAGE = "img/no_image.gif";
	private transient byte[] pictureBytes;
	private transient SakaiProxy sakaiProxy;
	
	public ProfilePopupPage(PageParameters params)
	{
		super(params);
		
		if(logger.isDebugEnabled()) logger.debug("ProfilePopupPage()");
		
		String userId = params.getString("userId");
		if(logger.isDebugEnabled()) logger.debug("User ID: " + userId);
		
		sakaiProxy = BlogApplication.get().getSakaiProxy();
		
		Profile profile = sakaiProxy.getProfile(userId);
		
		if(profile != null)
		{
			pictureBytes = profile.getInstitutionalPicture();
		}
		
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
		{
			add(new Image("photo",new ContextRelativeResource(UNAVAILABLE_IMAGE)));
		}
	}
}
