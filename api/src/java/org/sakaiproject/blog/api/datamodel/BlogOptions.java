package org.sakaiproject.blog.api.datamodel;

import java.io.Serializable;

public class BlogOptions  implements Serializable
{
	private String siteId;
	private String mode = Modes.BLOG;
	private int timeoutDays = 0;
	private int timeoutHours = 0;

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
	
	public void setTimeoutHours(String hours)
	{
		timeoutHours = Integer.parseInt(hours);
	}
	
	public String getTimeoutHours()
	{
		return Integer.toString(timeoutHours);
	}
	
	public void setTimeoutDays(String days)
	{
		timeoutDays = Integer.parseInt(days);
	}
	
	public String getTimeoutDays()
	{
		return Integer.toString(timeoutDays);
	}
}
