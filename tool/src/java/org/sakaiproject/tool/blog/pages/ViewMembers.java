package org.sakaiproject.tool.blog.pages;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.tool.blog.BlogApplication;
import org.sakaiproject.tool.blog.api.BlogFunctions;
import org.sakaiproject.tool.blog.api.BlogMember;
import org.sakaiproject.tool.blog.dataproviders.SiteMembersDataProvider;
import org.sakaiproject.tool.blog.impl.managers.PersistenceManager;
import org.sakaiproject.tool.blog.pages.models.MemberModel;

public class ViewMembers extends BasePage
{
	//private String sort;
	//private String direction;
	
	public ViewMembers()
	{
		this("userDisplayName","ascending");
	}
	
	public ViewMembers(final String sort,final String direction)
	{
		//sort = sortColumn;
		//direction = dir;
		
		PersistenceManager pm = BlogApplication.get().getPersistenceManager();
		if(pm.getOptions().isLearningLogMode())
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
		
		dataView.setItemsPerPage(10);

		PagingNavigator nav = new PagingNavigator("memberNavigator", dataView);

		add(nav);

		add(dataView);
	}
}
