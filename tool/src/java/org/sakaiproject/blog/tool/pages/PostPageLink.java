package org.sakaiproject.blog.tool.pages;

import org.apache.wicket.markup.html.link.Link;
import org.sakaiproject.blog.tool.pages.models.PostModel;

abstract class PostPageLink extends Link
{
	protected PostModel postModel;

	public PostPageLink(String id)
	{
		super(id);
	}
	
	public void setPostModel(PostModel postModel)
	{
		this.postModel = postModel;
	}
}
