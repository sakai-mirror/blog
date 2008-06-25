package org.sakaiproject.tool.blog.pages;

import java.text.DateFormat;
import java.util.Locale;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.tool.blog.api.BlogMember;
import org.sakaiproject.tool.blog.dataproviders.PublicBloggerDataProvider;
import org.sakaiproject.tool.blog.dataproviders.SiteMembersDataProvider;
import org.sakaiproject.tool.blog.pages.models.MemberModel;

public class ViewPublicBloggers extends BasePage
{
	public ViewPublicBloggers()
	{
		viewMembersLink.setVisible(false);
		newPostLink.setVisible(false);
		
		add(new Label("blogsLabel",new ResourceModel("blogsLabel")));
		
		add(new Label("memberHeader",new ResourceModel("member")));
		add(new Label("postsHeader",new ResourceModel("posts")));
		add(new Label("dateOfLastPostHeader",new ResourceModel("dateOfLastPostHeader")));
		
		PublicBloggerDataProvider provider = new PublicBloggerDataProvider();

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
