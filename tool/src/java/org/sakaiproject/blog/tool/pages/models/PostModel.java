package org.sakaiproject.blog.tool.pages.models;

import org.apache.log4j.Logger;
import org.apache.wicket.model.IModel;
import org.sakaiproject.blog.tool.BlogApplication;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.BlogManager;

public class PostModel implements IModel
{
	private transient Logger logger = Logger.getLogger(PostModel.class);
	
	private String postId;
	private transient Post post;
	private transient BlogManager blogManager;
	
	public PostModel(Post post)
	{
		this.post = post;
		this.postId = post.getId();
		
		blogManager = BlogApplication.get().getBlogManager();
	}
	
	public PostModel(String postId)
	{
		this.postId = postId;
		
		blogManager = BlogApplication.get().getBlogManager();
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
				post = blogManager.getPost(postId);
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
