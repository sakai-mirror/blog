package org.sakaiproject.blog.tool.pages;

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
			if(persistenceManager.getOptions().isLearningLogMode())
			{
				if(sakaiProxy.isCurrentUserMaintainer() || securityManager.isCurrentUserTutor())
					setResponsePage(new ViewMembers());
				else
					setResponsePage(new MemberBlog());
			}
			else
				setResponsePage(new ViewAll());
		}
	}
}
