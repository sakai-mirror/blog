package org.sakaiproject.blog.api;

import java.io.Serializable;

import org.sakaiproject.user.api.User;

public class BlogMember implements Serializable,Comparable
{
	private int numberOfPosts = 0;

	private long dateOfLastPost;
	
	private long dateOfLastComment;
	
	private String lastCommentCreator;

	private transient User sakaiUser = null;
	
	public BlogMember(User user)
	{
		this.sakaiUser = user;
	}

	public String getUserId()
	{
		return sakaiUser.getId();
	}

	public String getUserEid()
	{
		return sakaiUser.getEid();

	}

	public String getUserDisplayName()
	{
		return sakaiUser.getLastName() + ", " + sakaiUser.getFirstName();
		//return sakaiUser.getDisplayName();
	}

	public void setNumberOfPosts(int numberOfPosts)
	{
		this.numberOfPosts = numberOfPosts;
	}

	public int getNumberOfPosts()
	{
		return numberOfPosts;
	}

	public void setDateOfLastPost(long last)
	{
		this.dateOfLastPost = last;
	}

	public long getDateOfLastPost()
	{
		return dateOfLastPost;
	}

	public int compareTo(Object that)
	{
		
		String thisName = getUserDisplayName();
		String  thatName = ((BlogMember)that).getUserDisplayName();
		
		return thisName.compareToIgnoreCase(thatName);
	}

	public void setDateOfLastComment(long dateOfLastComment)
	{
		this.dateOfLastComment = dateOfLastComment;
	}

	public long getDateOfLastComment()
	{
		return dateOfLastComment;
	}

	public void setLastCommentCreator(String lastCommentCreator)
	{
		this.lastCommentCreator = lastCommentCreator;
	}

	public String getLastCommentCreator()
	{
		return lastCommentCreator;
	}
}