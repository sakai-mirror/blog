package org.sakaiproject.blog.tool.pages;

import java.text.DateFormat;
import java.util.Locale;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
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
	//private String sort;
	//private String direction;
	
	//private ProfilePanel profilePanel;
	
	private static final String UNAVAILABLE_IMAGE = "img/officialPhotoUnavailable.jpg";
	
	private Label displayNameLabel;
	//private Label blurbLabel;
	private NonCachingImage photo;

	public ViewMembers()
	{
		this("userDisplayName","ascending");
	}
	
	public ViewMembers(final String sort,final String direction)
	{
		//sort = sortColumn;
		//direction = dir;
		
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
		
		Link sortByMemberLink = new Link("sortByMemberLink")
		{
			public void onClick()
			{
				if(direction.equals("descending"))
					setResponsePage(new ViewMembers("userDisplayName","ascending"));
				else
					setResponsePage(new ViewMembers("userDisplayName","descending"));
				
			}
		};
		sortByMemberLink.add(new Label("memberHeader",memberModel));
		add(sortByMemberLink);
		
		Link sortByPostTotalLink = new Link("sortByPostTotalLink")
		{
			public void onClick()
			{
				if(direction.equals("descending"))
					setResponsePage(new ViewMembers("numberOfPosts","ascending"));
				else
					setResponsePage(new ViewMembers("numberOfPosts","descending"));
			}
		};
		sortByPostTotalLink.add(new Label("postsHeader",postsModel));
		add(sortByPostTotalLink);
		
		Link sortByLastPostDateLink = new Link("sortByLastPostDateLink")
		{
			public void onClick()
			{
				if(direction.equals("descending"))
					setResponsePage(new ViewMembers("dateOfLastPost","ascending"));
				else
					setResponsePage(new ViewMembers("dateOfLastPost","descending"));
				
			}
		};
		sortByLastPostDateLink.add(new Label("dateOfLastPostHeader",dateModel));
		add(sortByLastPostDateLink);
		
		Link sortByLastCommentDateLink = new Link("sortByLastCommentDateLink")
		{
			public void onClick()
			{
				if(direction.equals("descending"))
					setResponsePage(new ViewMembers("dateOfLastComment","ascending"));
				else
					setResponsePage(new ViewMembers("dateOfLastComment","descending"));
			}
		};
		sortByLastCommentDateLink.add(new Label("dateOfLastCommentHeader",commentModel));
		add(sortByLastCommentDateLink);
		
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
				
				showPostsLink.add(new OnMouseOverBehaviour());
				showPostsLink.add(new AttributeAppender("onMouseOut", new Model("hideProfilePopup(event);"),";"));

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
		
		dataView.setItemsPerPage(10);

		PagingNavigator nav = new PagingNavigator("memberNavigator", dataView);

		add(nav);

		add(dataView);
		
		/*
		Image indicator = new Image("indicator",AbstractDefaultAjaxBehavior.INDICATOR);
		indicator.setOutputMarkupId(true);
		add(indicator);
		*/
		
		addProfilePanelComponents();
	}
	
	private void addProfilePanelComponents()
	{
		displayNameLabel = new Label("displayName","");
		displayNameLabel.setOutputMarkupId(true);
		add(displayNameLabel);
		
		//String blurb = "";
		
		//blurbLabel = new Label("profileBlurb","");
		//blurbLabel.setOutputMarkupId(true);
		//blurbLabel.setEscapeModelStrings(false);
		//add(blurbLabel);
		
		photo = new NonCachingImage("photo",new ContextRelativeResource(UNAVAILABLE_IMAGE));
		photo.setOutputMarkupId(true);
		add(photo);
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
	
	private class OnMouseOverBehaviour extends AjaxEventBehavior// implements IAjaxIndicatorAware
	{
		public OnMouseOverBehaviour()
		{
			super("onMouseOver");
		}
		
		protected void onEvent(AjaxRequestTarget target)
		{
			Link link = (Link) getComponent();
			BlogMember member = (BlogMember) link.getModelObject();
			setUserId(member.getUserId());
			target.addComponent(displayNameLabel);
			//target.addComponent(blurbLabel);
			target.addComponent(photo);
		}
		
		public IAjaxCallDecorator getAjaxCallDecorator()
		{
			return new AjaxCallDecorator()
			{
				public CharSequence decorateScript(CharSequence script)
				{
					return "moveProfilePopup(event);" + script;
				}
			};
		}
		
		protected CharSequence getSuccessScript()
		{
			return "showProfilePopup()";
		}

		public String getAjaxIndicatorMarkupId()
		{
			return "indicator";
		}
	}
}
