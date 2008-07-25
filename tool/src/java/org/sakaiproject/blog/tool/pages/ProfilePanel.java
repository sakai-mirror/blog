package org.sakaiproject.blog.tool.pages;

import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.resource.ContextRelativeResource;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.blog.tool.BlogApplication;
import org.sakaiproject.blog.api.SakaiProxy;

public class ProfilePanel extends Panel
{
	private static final String UNAVAILABLE_IMAGE = "img/officialPhotoUnavailable.jpg";
	private transient byte[] pictureBytes;
	private String userId;
	private transient SakaiProxy sakaiProxy;
	private Label displayNameLabel;
	private Label blurbLabel;
	private Image photo;
	
	public ProfilePanel(String id,String uId)
	{
		super(id);
		
		this.userId = uId;
		
		sakaiProxy = BlogApplication.get().getSakaiProxy();
		
		String displayName
			= sakaiProxy.getDisplayNameForTheUser(userId);
		
		displayNameLabel = new Label("displayName",displayName);
		add(displayNameLabel);
		
		String blurb = "";
		
		Profile profile = sakaiProxy.getProfile(userId);
		
		if(profile != null)
		{
			blurb = profile.getOtherInformation();
			pictureBytes = profile.getInstitutionalPicture();
		}
		
		blurbLabel = new Label("profileBlurb",blurb);
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
		
			photo = new Image("photo",photoResource);
			add(photo);
		}
		else
		{
			photo = new Image("photo",new ContextRelativeResource(UNAVAILABLE_IMAGE));
			add(photo);
		}
	}
	
	public void setUserId(String userId)
	{
		this.userId = userId;
		
		String displayName
			= sakaiProxy.getDisplayNameForTheUser(userId);
		displayNameLabel.setModelObject(displayName);
		
		Profile profile = sakaiProxy.getProfile(userId);
		
		if(profile != null)
		{
			String blurb = profile.getOtherInformation();
			blurbLabel.setModelObject(blurb);
			pictureBytes = profile.getInstitutionalPicture();
		
			if(pictureBytes != null && pictureBytes.length > 0)
			{
				BufferedDynamicImageResource photoResource = new BufferedDynamicImageResource()
				{
					protected byte[] getImageData()
					{
						return pictureBytes;
					}
				};
		
				photo.setImageResource(photoResource);
				photo.renderComponent();
			}
			else
			{
				photo.setImageResource(new ContextRelativeResource(UNAVAILABLE_IMAGE));
			}
		}
		
		//this.renderComponent();
	}
	
	protected void onRender(MarkupStream markup)
	{
		super.onRender(markup);
		
		System.out.println("RENDER !!!");
	}
}
