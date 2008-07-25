package org.sakaiproject.blog.tool.pages;

import java.text.DateFormat;
import java.util.Locale;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.blog.api.BlogMember;
import org.sakaiproject.blog.tool.dataproviders.PublicBloggerDataProvider;
import org.sakaiproject.blog.tool.pages.models.MemberModel;

public class ViewPublicBloggers extends BasePage
{
	public ViewPublicBloggers()
	{
		this("userDisplayName","ascending");
	}
	
	public ViewPublicBloggers(final String sort,final String direction)
	{
		viewMembersLink.setVisible(false);
		newPostLink.setVisible(false);
		
		add(new Label("blogsLabel",new ResourceModel("blogsLabel")));
		
		ResourceModel memberModel = new ResourceModel("member");
		ResourceModel dateModel = new ResourceModel("dateOfLastPostHeader");
		ResourceModel postsModel = new ResourceModel("posts");
		
		Link sortByMemberLink = new Link("sortByMemberLink")
		{
			public void onClick()
			{
				if(direction.equals("descending"))
					setResponsePage(new ViewPublicBloggers("userDisplayName","ascending"));
				else
					setResponsePage(new ViewPublicBloggers("userDisplayName","descending"));
			}
		};
		sortByMemberLink.add(new Label("memberHeader",memberModel));
		add(sortByMemberLink);
		
		Link sortByPostTotalLink = new Link("sortByPostTotalLink")
		{
			public void onClick()
			{
				if(direction.equals("descending"))
					setResponsePage(new ViewPublicBloggers("numberOfPosts","ascending"));
				else
					setResponsePage(new ViewPublicBloggers("numberOfPosts","descending"));
			}
		};
		sortByPostTotalLink.add(new Label("postsHeader",postsModel));
		add(sortByPostTotalLink);
		
		Link sortByLastPostDateLink = new Link("sortByLastPostDateLink")
		{
			public void onClick()
			{
				if(direction.equals("descending"))
					setResponsePage(new ViewPublicBloggers("dateOfLastPost","ascending"));
				else
					setResponsePage(new ViewPublicBloggers("dateOfLastPost","descending"));
				
			}
		};
		sortByLastPostDateLink.add(new Label("dateOfLastPostHeader",dateModel));
		add(sortByLastPostDateLink);
		
		PublicBloggerDataProvider provider = new PublicBloggerDataProvider(sort,direction);

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

				showPostsLink.add(new Label("name", member.getUserDisplayName()));

				item.add(showPostsLink);
				
				item.add(new Label("postCount",Integer.toString(member.getNumberOfPosts())));
				
				long last = member.getDateOfLastPost();
				if(last == 0L)
					item.add(new Label("dateOfLastPost","n/a"));
				else
				{
					String lastDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM,Locale.UK).format(last);
					item.add(new Label("dateOfLastPost",lastDate));
				}
			}
		};
		
		dataView.setItemsPerPage(5);

		PagingNavigator nav = new PagingNavigator("memberNavigator", dataView);

		add(nav);

		add(dataView);
	}
}
