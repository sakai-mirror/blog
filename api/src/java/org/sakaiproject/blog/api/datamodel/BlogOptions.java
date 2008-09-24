package org.sakaiproject.blog.api.datamodel;

import java.io.Serializable;

import org.apache.log4j.Logger;

public class BlogOptions  implements Serializable
{
	private transient Logger logger = Logger.getLogger(BlogOptions.class);
	
	private String siteId;
	private String mode = Modes.BLOG;

	public boolean isLearningLogMode()
	{
		return mode.equals(Modes.LEARNING_LOG);
	}

	public void setSiteId(String placementId)
	{
		this.siteId = placementId;
	}

	public String getSiteId()
	{
		return siteId;
	}

	public void setMode(String mode)
	{
		this.mode = mode;
	}

	public String getMode()
	{
		return mode;
	}
}
