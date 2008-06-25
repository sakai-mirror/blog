package org.sakaiproject.tool.blog.pages;

import org.sakaiproject.tool.blog.BlogApplication;
import org.sakaiproject.tool.blog.api.BlogFunctions;
import org.sakaiproject.tool.blog.impl.managers.PersistenceManager;

public class Dispatcher extends BasePage
{
	public Dispatcher()
	{
		super();
		
		if(sakaiProxy.isOnGateway())
		{
			setResponsePage(new ViewPublicBloggers());
		}
		else
		{
			PersistenceManager pm = BlogApplication.get().getPersistenceManager();
			if(pm.getOptions().isLearningLogMode())
			{
				if(sakaiProxy.isCurrentUserMaintainer())
					setResponsePage(new ViewMembers());
				else if(sakaiProxy.isAllowedFunction(BlogFunctions.BLOG_POST_READ_OWN))
					setResponsePage(new MemberBlog());
				else
					setResponsePage(new ViewMembers());
			}
			else
				setResponsePage(new ViewAll());
		}
	}
}
