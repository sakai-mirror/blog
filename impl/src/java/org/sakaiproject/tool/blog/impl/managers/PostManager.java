/*************************************************************************************
 Copyright (c) 2006. Centre for e-Science. Lancaster University. United Kingdom.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 *************************************************************************************/

package org.sakaiproject.tool.blog.impl.managers;

import java.util.List;

import org.apache.log4j.Logger;
import org.sakaiproject.tool.blog.api.BlogMember;
import org.sakaiproject.tool.blog.api.QueryBean;
import org.sakaiproject.tool.blog.api.datamodel.Comment;
import org.sakaiproject.tool.blog.api.datamodel.File;
import org.sakaiproject.tool.blog.api.datamodel.Image;
import org.sakaiproject.tool.blog.api.datamodel.Post;
import org.sakaiproject.tool.blog.api.datamodel.PostElement;
import org.sakaiproject.tool.blog.api.datamodel.State;

public class PostManager
{
	private transient Logger logger = Logger.getLogger(PostManager.class);

	private transient PersistenceManager persistenceManager;

	private transient BlogSecurityManager securityManager;

	public void setSecurityManager(BlogSecurityManager securityManager)
	{
		this.securityManager = securityManager;
	}

	public void setPersistenceManager(PersistenceManager pm)
	{
		this.persistenceManager = pm;
	}

	public Post getPost(String postId) throws Exception
	{
		return persistenceManager.getPost(postId);
	}

	public List<Post> getPosts(String placementId) throws Exception
	{
		// Get all the posts for the supplied site and filter them through the
		// security manager
		List<Post> filtered;
		List<Post> unfiltered = persistenceManager.getAllPost(placementId);
		filtered = securityManager.filterSearch(unfiltered);
		return filtered;
	}

	public List<Post> getPosts(QueryBean query) throws Exception
	{
		// Get all the posts for the supplied site and filter them through the
		// security manager
		List<Post> filtered;
		List<Post> unfiltered = persistenceManager.getPosts(query);
		filtered = securityManager.filterSearch(unfiltered);
		return filtered;
	}

	public void createPost(Post post) throws Exception
	{
		persistenceManager.createPost(post);
	}
	
	public void savePost(Post post) throws Exception
	{
		persistenceManager.savePost(post);
	}

	public void deletePost(String postId)
	{
		try
		{
			Post post = persistenceManager.getPost(postId);
			if (securityManager.canCurrentUserDeletePost(post))
			{
				persistenceManager.deletePost(postId);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public Image getImage(String imageId, int mode) throws PersistenceException
	{
		return persistenceManager.getImage(imageId, mode);
	}

	public File getFile(String fileId) throws PersistenceException
	{
		// TODO Auto-generated method stub
		return persistenceManager.getFile(fileId);
	}

	public void addComment(Post post,Comment comment)
	{
		post.addComment(comment);
		persistenceManager.addComment(comment);
	}
	
	public void deleteComment(Post post,Comment comment)
	{
		post.removeComment(comment);
		persistenceManager.deleteComment(comment);
	}

	public void updateComment(Post post, Comment comment)
	{
		persistenceManager.updateComment(comment);
		//post.replaceComment(comment);
	}

	public void removeResourceReference(String resourceId)
	{
		persistenceManager.removeResourceReference(resourceId);
	}

	public void replaceElement(Post post, PostElement element, int elementIndex)
	{
		try
		{
			persistenceManager.updatePostElement(post,element,elementIndex);
			post.replaceElement(element, elementIndex);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst replacing element",e);
		}
	}

	public void addElement(Post post, PostElement element, int elementIndex)
	{
		try
		{
			post.addElement(element, elementIndex);
			persistenceManager.addPostElement(post,element);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst adding element",e);
			
			post.removeElement(elementIndex);
		}
	}
	
	public void addElement(Post post, PostElement element)
	{
		try
		{
			post.addElement(element);
			persistenceManager.addPostElement(post,element);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst adding element",e);
			
			post.removeElement(element);
		}
	}

	public void setIndentation(Post post, PostElement element, int i)
	{
		try
		{
			//post.addElement(element, elementIndex);
			persistenceManager.setIndentation(post,element,i);
			element.setIndentation(i);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst adding element",e);
			
			//post.removeElement(elementIndex);
		}
	}

	public void moveUp(Post post, PostElement bottomElement)
	{
		try
		{
			int bottom = post.getElementPosition(bottomElement);
			int top = bottom - 1;
			persistenceManager.swapElements(post,top,bottom);
			PostElement topElement = post.getElement(top);
			post.replaceElement(bottomElement, top);
			post.replaceElement(topElement, bottom);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst moving element up",e);
		}
	}
	
	public void moveDown(Post post, PostElement topElement)
	{
		try
		{
			int top = post.getElementPosition(topElement);
			int bottom = top + 1;
			persistenceManager.swapElements(post,top,bottom);
			PostElement bottomElement = post.getElement(bottom);
			post.replaceElement(bottomElement, top);
			post.replaceElement(topElement, bottom);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst moving element down",e);
		}
	}

	public void deleteElement(Post post, int index)
	{
		try
		{
			persistenceManager.deleteElement(post, index);
			post.removeElement(index);
		}
		catch(Exception e)
		{
			logger.error("Caught exception whilst deleting element",e);
		}
	}

	public void saveTitle(Post post)
	{
		try
		{
			persistenceManager.saveTitle(post);
		}
		catch(Exception e)
		{
			logger.error("Caught exception whilst saving title",e);
		}
	}

	public void saveShortText(Post post)
	{
		try
		{
			persistenceManager.saveShortText(post);
		}
		catch(Exception e)
		{
			logger.error("Caught exception whilst saving short text",e);
		}
	}

	public void saveReadOnly(Post post)
	{
		try
		{
			persistenceManager.saveReadOnly(post);
		}
		catch(Exception e)
		{
			logger.error("Caught exception whilst saving read only",e);
		}
	}

	public void saveAllowComments(Post post)
	{
		try
		{
			persistenceManager.saveAllowComments(post);
		}
		catch(Exception e)
		{
			logger.error("Caught exception whilst saving allow comments",e);
		}
	}

	public void saveVisibility(Post post)
	{
		try
		{
			persistenceManager.saveVisibility(post);
		}
		catch(Exception e)
		{
			logger.error("Caught exception whilst saving visibility",e);
		}
	}

	public void recyclePost(String postId)
	{
		try
		{
			Post post = persistenceManager.getPost(postId);
			
			if (securityManager.canCurrentUserDeletePost(post))
			{
				persistenceManager.recyclePost(postId);
				post.setVisibility(State.RECYCLED);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
