package org.sakaiproject.blog.tool.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.sakaiproject.blog.api.BlogFunctions;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.tool.pages.models.PostModel;

public class PostPage extends BasePage
{
	public PostPage(PostModel postModel)
	{
		this(postModel,false);
	}
	
	public PostPage(PageParameters params)
	{
		this(new PostModel(params.getString("postId")),false);
	}
	
	public PostPage(PostModel postModel,boolean editingMode)
	{
		super();
		
		if(persistenceManager.getOptions().isLearningLogMode())
		{
			if(sakaiProxy.isAllowedFunction(BlogFunctions.BLOG_POST_READ_OWN))
			{
				viewAllLink.setVisible(false);
				viewMembersLink.setVisible(false);
			}
		}
		
		final String creatorId = ((Post) postModel.getObject()).getCreatorId();
		
		Link memberPageLink = new Link("memberPageLink")
		{
			public void onClick()
			{
				setResponsePage(new MemberBlog(creatorId));
			}
		};
		
		String displayName
			= sakaiProxy.getDisplayNameForTheUser(creatorId);
		
		memberPageLink.add(new Label("memberLabel",displayName + "'s Blog"));
		
		add(memberPageLink);
		
		setupPanel(postModel,editingMode);
	}
	
	private void setupPanel(PostModel postModel,boolean editingMode)
	{
		PostPanel postPanel = new PostPanel("postPanel", postModel,0,editingMode);
		
		Post post = (Post) postModel.getObject();
		if(post.isRecycled())
			postPanel.add(new AttributeAppender("style", true,new Model("opacity: 0.3;filter:alpha(opacity=30);"),";"));
		
		postPanel.setShowComments(true);
		postPanel.setShowFullContent(true);
		add(postPanel);
	}
}
