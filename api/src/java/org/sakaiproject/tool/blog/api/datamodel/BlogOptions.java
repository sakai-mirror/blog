package org.sakaiproject.tool.blog.api.datamodel;

import java.io.Serializable;

public class BlogOptions  implements Serializable
{
	private String siteId;
	
	private String mode = Modes.BLOG;

	public boolean isLearningLogMode()
	{
		return mode.equals(Modes.LEARNING_LOG);
	}

	public void setMode(String mode)
	{
		this.mode = mode;
	}

	public String getMode()
	{
		return mode;
	}

	public void setSiteId(String placementId)
	{
		this.siteId = placementId;
	}

	public String getSiteId()
	{
		return siteId;
	}
}
