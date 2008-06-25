package org.sakaiproject.tool.blog.pages.models;

import org.apache.log4j.Logger;
import org.apache.wicket.model.IModel;
import org.sakaiproject.tool.blog.BlogApplication;
import org.sakaiproject.tool.blog.api.datamodel.Post;
import org.sakaiproject.tool.blog.impl.managers.PostManager;

public class PostModel implements IModel
{
	private transient Logger logger = Logger.getLogger(PostModel.class);
	
	private String postId;
	private transient Post post;
	private transient PostManager postManager;
	
	public PostModel(Post post)
	{
		this.post = post;
		this.postId = post.getId();
		
		postManager = BlogApplication.get().getPostManager();
	}
	
	public PostModel(String postId)
	{
		this.postId = postId;
		
		postManager = BlogApplication.get().getPostManager();
	}

	public Object getObject()
	{
		if(logger.isDebugEnabled()) logger.debug("getObject()");
		
		if(post == null)
		{
			if(logger.isDebugEnabled())
				logger.debug("The post is null. Loading it from the post manager ...");
			
			try
			{
				post = postManager.getPost(postId);
			}
			catch (Exception e)
			{
				logger.error("Caught exception whilst loading post",e);
			}
		}
		
		return post;
	}

	public void setObject(Object object)
	{
		if(object instanceof Post)
			post = (Post) object;
		else if(object == null)
			post = null;
		else
			System.out.println("Failed to setObject on this PostModel. The object needs to be a Post.");
	}

	public void detach() {}
}
