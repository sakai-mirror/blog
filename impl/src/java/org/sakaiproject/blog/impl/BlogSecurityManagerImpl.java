/*************************************************************************************
 * Copyright 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.

 *************************************************************************************/

package org.sakaiproject.blog.impl;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.blog.api.BlogFunctions;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blog.api.BlogSecurityManager;
import org.sakaiproject.blog.api.PersistenceManager;
import org.sakaiproject.blog.api.datamodel.Comment;
import org.sakaiproject.blog.api.datamodel.Post;

public class BlogSecurityManagerImpl implements BlogSecurityManager
{
	private SakaiProxy sakaiProxy;

    private PersistenceManager persistenceManager;

    public void init()
    {
    }

    public boolean canCurrentUserCommentOnPost(Post post)
	{
		//if(sakaiProxy.isOnGateway() && post.isPublic() && post.isCommentable())
			//return true;
		
		// This acts as an override
		if(!sakaiProxy.isAllowedFunction(BlogFunctions.BLOG_COMMENT_CREATE))
			return false;
		
		String currentUser = sakaiProxy.getCurrentUserId();
			
		if(post.isCommentable() || post.getCreatorId().equals(currentUser))
			return true;
		
		if(persistenceManager.getOptions().isLearningLogMode()
				&& isCurrentUserTutor())
		{
			return true;
		}
		
		return false;
	}
	
	public boolean isCurrentUserTutor()
	{
		if(sakaiProxy.isAllowedFunction(BlogFunctions.BLOG_COMMENT_CREATE)
				&& !sakaiProxy.isAllowedFunction(BlogFunctions.BLOG_POST_CREATE))
		{
			return true;
		}
		
		return false;
	}
	
	public boolean canCurrentUserDeletePost(Post post) throws SecurityException
	{
		if(sakaiProxy.isAllowedFunction(BlogFunctions.BLOG_POST_DELETE_ANY))
			return true;
		
		String currentUser = sakaiProxy.getCurrentUserId();
		
		if(currentUser != null && currentUser.equals(post.getCreatorId()))
		{
			if(sakaiProxy.isAllowedFunction(BlogFunctions.BLOG_POST_DELETE_OWN))
				return true;
		}
		
		return false;
	}
	
	public boolean canCurrentUserEditPost(Post post)
	{
		// This acts as an override
		if(sakaiProxy.isAllowedFunction(BlogFunctions.BLOG_POST_UPDATE_ANY))
			return true;
		
		String currentUser = sakaiProxy.getCurrentUserId();
		
		// If it's public and not marked read only, yes.
		if(post.isPublic() && !post.isReadOnly())
			return true;
		
		// If the current user is authenticated and the post author, yes.
		if(currentUser != null && currentUser.equals(post.getCreatorId()))
		{
			if(sakaiProxy.isAllowedFunction(BlogFunctions.BLOG_POST_UPDATE_OWN))
				return true;
		}
		
		// If the user is authenticated and the post is not marked read only,
		// yes
		if(currentUser != null && !post.isReadOnly())
			return true;
		
		return false;
	}

	public List<Post> filterSearch(List<Post> posts)
	{
		List<Post> filtered = new ArrayList<Post>();
		for(Post post : posts)
		{
			if(canCurrentUserReadPost(post))
				filtered.add(post);
		}
		
		return filtered;
	}
	
	public boolean canCurrentUserReadPost(Post post)
	{
		// If the post is public, yes.
		if(post.isPublic())
			return true;
		
		// Only maintainers can view recycled posts
		if(post.isRecycled() && !sakaiProxy.isCurrentUserMaintainer())
			return false;
		
		// This acts as an override
		if(!post.isPrivate() && sakaiProxy.isAllowedFunction(BlogFunctions.BLOG_POST_READ_ANY))
		{
			return true;
		}
		
		String currentUser = sakaiProxy.getCurrentUserId();
		
		if(sakaiProxy.isCurrentUserAdmin())
			return true;
		
		// If the current user is authenticated and the post author, yes.
		if(currentUser != null && currentUser.equals(post.getCreatorId()))
		{
			if(sakaiProxy.isAllowedFunction(BlogFunctions.BLOG_POST_READ_OWN))
				return true;
		}
		
		String siteId = sakaiProxy.getCurrentSiteId();
		
		// If the current user is authenticated and the post belongs to the
		// current site and the post is SITE visible, yes.
		if(currentUser != null
				&& siteId != null
				&& siteId.equals(post.getSiteId()))
		{
			if(!post.isPrivate() && sakaiProxy.isAllowedFunction(BlogFunctions.BLOG_POST_READ_ANY))
				return true;
		}
		
		return false;
	}
	
	public boolean canCurrentUserDeleteComment(Post post,Comment comment)
	{
		if(sakaiProxy.isAllowedFunction(BlogFunctions.BLOG_COMMENT_DELETE_ANY))
			return true;
		
		// If I am the owner of the post commented upon, allow me.
		if(post.getCreatorId().equals(sakaiProxy.getCurrentUserId()))
				return true;
		
		// If I am the owner of the comment, allow me.
		if(comment.getCreatorId().equals(sakaiProxy.getCurrentUserId()))
		{
			if(sakaiProxy.isAllowedFunction(BlogFunctions.BLOG_COMMENT_DELETE_OWN))
				return true;
		}
		
		return false;
	}

	/*
	public void setPersistenceManager(PersistenceManagerImpl persistenceManager)
	{
		this.persistenceManager = persistenceManager;
	}
	*/

	public void setSakaiProxy(SakaiProxy sakaiProxy)
	{
		this.sakaiProxy = sakaiProxy;
	}

	public SakaiProxy getSakaiProxy()
	{
		return sakaiProxy;
	}

	public boolean canCurrentUserCreatePosts()
	{
		return sakaiProxy.isAllowedFunction(BlogFunctions.BLOG_POST_CREATE);
	}

	public boolean canCurrentUserEditComment(Post post, Comment comment)
	{
		if(sakaiProxy.isAllowedFunction(BlogFunctions.BLOG_COMMENT_UPDATE_ANY))
			return true;
		
		// If I am the owner of the comment, allow me.
		if(comment.getCreatorId().equals(sakaiProxy.getCurrentUserId()))
		{
			if(sakaiProxy.isAllowedFunction(BlogFunctions.BLOG_COMMENT_UPDATE_OWN))
				return true;
		}
		
		return false;
	}

	public void setPersistenceManager(PersistenceManager persistenceManager)
	{
		this.persistenceManager = persistenceManager;
	}

    public PersistenceManager getPersistenceManager()
    {
        return persistenceManager;
    }

    public boolean canCurrentUserSearch()
	{
		if(persistenceManager.getOptions().isLearningLogMode())
		{
			if(isCurrentUserTutor() || sakaiProxy.isCurrentUserMaintainer())
				return true;
		}
		else
			return true;
		
		return false;
	}
}
