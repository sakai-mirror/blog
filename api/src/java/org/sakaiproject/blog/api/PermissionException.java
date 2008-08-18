package org.sakaiproject.blog.api;

public class PermissionException extends RuntimeException
{
	public PermissionException(String message)
	{
		super(message);
	}
	
	public PermissionException(String message, Throwable t)
	{
		super(message,t);
	}
}
