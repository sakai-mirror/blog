package org.sakaiproject.blog.tool.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.tool.pages.models.PostModel;

public class EditPost extends BasePage
{
	private Post post;
	
	public EditPost()
	{
		this(new Post());
		
		// This is a brand new post so we need to set its siteId
		String sId = sakaiProxy.getCurrentSiteId();
		post.setSiteId(sId);
		post.setCreatorId(sakaiProxy.getCurrentUserId());
	}
	
	public EditPost(Post post)
	{
		this.post = post;
		
		newPostLink.setVisible(false);
		
		add(new FeedbackPanel("feedback"));
		add(new Label("postEditorLabel",new ResourceModel("postEditor")));
		add(new EditPostForm("postForm",new PostModel(post)));
	}
}
