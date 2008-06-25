package org.sakaiproject.tool.blog.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.tool.blog.BlogApplication;
import org.sakaiproject.tool.blog.api.datamodel.Post;
import org.sakaiproject.tool.blog.impl.managers.PersistenceManager;
import org.sakaiproject.tool.blog.pages.models.PostModel;

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
		
		PersistenceManager pm = BlogApplication.get().getPersistenceManager();
		if(pm.getOptions().isLearningLogMode())
			post.setCommentable(true);
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
