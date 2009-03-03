package org.sakaiproject.blog.tool.pages;

import java.text.DateFormat;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.resource.ContextRelativeResource;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.blog.api.BlogFunctions;
import org.sakaiproject.blog.api.BlogMember;
import org.sakaiproject.blog.tool.dataproviders.SiteMembersDataProvider;
import org.sakaiproject.blog.tool.pages.models.MemberModel;

public class ViewMembers extends BasePage
{
	private static final String UNAVAILABLE_IMAGE = "img/no_image.gif";
	
	private Label displayNameLabel;
	
	private NonCachingImage photo;

	public ViewMembers()
	{
		this("userDisplayName","ascending");
	}
	
	public ViewMembers(final String sort,final String direction)
	{
		super();
		
		if(persistenceManager.getOptions().isLearningLogMode())
		{
			if(!sakaiProxy.isAllowedFunction(BlogFunctions.BLOG_POST_READ_OWN))
			{
				viewAllLink.setVisible(false);
				myBlogLink.setVisible(false);
			}
		}
		viewMembersLink.setVisible(false);
		newPostLink.setVisible(false);
		
		add(new Label("bloggersLabel",new ResourceModel("listOfSiteMembers")));
		
		ResourceModel memberModel = new ResourceModel("member");
		ResourceModel dateModel = new ResourceModel("dateOfLastPostHeader");
		ResourceModel commentModel = new ResourceModel("dateOfLastCommentHeader");
		ResourceModel postsModel = new ResourceModel("posts");
		
		SiteMembersDataProvider provider = new SiteMembersDataProvider(sort,direction);
		
		add(new Label("memberHeader",memberModel));
		
		add(new Label("postsHeader",postsModel));
		
		add(new Label("dateOfLastPostHeader",dateModel));
		
		add(new Label("dateOfLastCommentHeader",commentModel));
		
		DataView dataView = new DataView("rows", provider)
		{

			@Override
			protected void populateItem(Item item)
			{
				BlogMember member = (BlogMember) item.getModelObject();

				Link showPostsLink = new Link("showPostsLink", new MemberModel(member))
				{
					@Override
					public void onClick()
					{
						BlogMember member = (BlogMember) getModelObject();
						setResponsePage(new MemberBlog(member.getUserId()));
					}
				};
				
				//String url = "?wicket:bookmarkablePage=:org.sakaiproject.blog.tool.pages.ProfilePopupPage&userId="
					//		+ member.getUserId();
				
				//showPostsLink.add(new AttributeModifier("rel", true, new Model(url)));

				showPostsLink.add(new Label("name", member.getUserDisplayName()));

				item.add(showPostsLink);
				
				item.add(new Label("postCount",Integer.toString(member.getNumberOfPosts())));
				
				long lastPost = member.getDateOfLastPost();
				if(lastPost == 0L)
					item.add(new Label("dateOfLastPost","n/a"));
				else
				{
					String lastDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM,Locale.UK).format(lastPost);
					item.add(new Label("dateOfLastPost",lastDate));
				}
				
				long lastComment = member.getDateOfLastComment();
				String lastCommentBy = sakaiProxy.getDisplayNameForTheUser(member.getLastCommentCreator());
				if(lastComment == 0L)
					item.add(new Label("dateOfLastComment","n/a"));
				else
				{
					String lastDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM,Locale.UK).format(lastComment);
					item.add(new Label("dateOfLastComment",lastDate + " (" + lastCommentBy + ")"));
				}
			}
		};
		
		add(dataView);
	}
	
	public void setUserId(String userId)
	{
		String displayName
			= sakaiProxy.getDisplayNameForTheUser(userId);
		displayNameLabel.setModelObject(displayName);
		
		Profile profile = sakaiProxy.getProfile(userId);
		
		if(profile != null)
		{
			//String blurb = profile.getOtherInformation();
			//blurbLabel.setModelObject(blurb);
			final byte[] pictureBytes = profile.getInstitutionalPicture();
		
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
			}
			else
			{
				photo.setImageResource(new ContextRelativeResource(UNAVAILABLE_IMAGE));
			}
		}
		else
			photo.setImageResource(new ContextRelativeResource(UNAVAILABLE_IMAGE));
	}
}
