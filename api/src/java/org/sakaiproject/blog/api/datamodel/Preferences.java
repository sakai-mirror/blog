package org.sakaiproject.blog.api.datamodel;

import java.io.Serializable;

public class Preferences implements Serializable
{
	private String siteId;
	private String userId;
	private boolean newPostAlert;
	private boolean newCommentAlert;
	
	public void setNewPostAlert(boolean newPostAlert)
	{
		this.newPostAlert = newPostAlert;
	}
	public boolean isNewPostAlert()
	{
		return newPostAlert;
	}
	public void setNewCommentAlert(boolean newCommentAlert)
	{
		this.newCommentAlert = newCommentAlert;
	}
	public boolean isNewCommentAlert()
	{
		return newCommentAlert;
	}
	public void setSiteId(String siteId)
	{
		this.siteId = siteId;
	}
	public String getSiteId()
	{
		return siteId;
	}
	public void setUserId(String userId)
	{
		this.userId = userId;
	}
	public String getUserId()
	{
		return userId;
	}

}
