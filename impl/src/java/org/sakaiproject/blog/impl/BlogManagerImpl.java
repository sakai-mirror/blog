package org.sakaiproject.blog.impl;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.sakaiproject.blog.api.BlogFunctions;
import org.sakaiproject.blog.api.BlogManager;
import org.sakaiproject.blog.api.BlogSecurityManager;
import org.sakaiproject.blog.api.PersistenceManager;
import org.sakaiproject.blog.api.QueryBean;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blog.api.XmlDefs;
import org.sakaiproject.blog.api.datamodel.BlogOptions;
import org.sakaiproject.blog.api.datamodel.Comment;
import org.sakaiproject.blog.api.datamodel.File;
import org.sakaiproject.blog.api.datamodel.Image;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.datamodel.PostElement;
import org.sakaiproject.blog.api.datamodel.State;
import org.sakaiproject.entity.api.*; //import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
//import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
//import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BlogManagerImpl implements BlogManager// , CoreEntityProvider, AutoRegisterEntityProvider
{
	private Logger logger = Logger.getLogger(BlogManagerImpl.class);

	private PersistenceManager persistenceManager;

	private BlogSecurityManager securityManager;

	private SakaiProxy sakaiProxy;

	public void init()
	{
		if (logger.isDebugEnabled())
			logger.debug("init()");

		logger.info("Registering Blog functions ...");

		sakaiProxy.registerFunction(BlogFunctions.BLOG_POST_CREATE);
		sakaiProxy.registerFunction(BlogFunctions.BLOG_POST_READ_ANY);
		sakaiProxy.registerFunction(BlogFunctions.BLOG_POST_READ_OWN);
		sakaiProxy.registerFunction(BlogFunctions.BLOG_POST_UPDATE_ANY);
		sakaiProxy.registerFunction(BlogFunctions.BLOG_POST_UPDATE_OWN);
		sakaiProxy.registerFunction(BlogFunctions.BLOG_POST_DELETE_ANY);
		sakaiProxy.registerFunction(BlogFunctions.BLOG_POST_DELETE_OWN);
		sakaiProxy.registerFunction(BlogFunctions.BLOG_COMMENT_CREATE);
		sakaiProxy.registerFunction(BlogFunctions.BLOG_COMMENT_READ_ANY);
		sakaiProxy.registerFunction(BlogFunctions.BLOG_COMMENT_READ_OWN);
		sakaiProxy.registerFunction(BlogFunctions.BLOG_COMMENT_UPDATE_ANY);
		sakaiProxy.registerFunction(BlogFunctions.BLOG_COMMENT_UPDATE_OWN);
		sakaiProxy.registerFunction(BlogFunctions.BLOG_COMMENT_DELETE_ANY);
		sakaiProxy.registerFunction(BlogFunctions.BLOG_COMMENT_DELETE_OWN);

		logger.info("Registered Blog functions ...");

		sakaiProxy.registerEntityProducer(this);
	}

	public Post getPost(String postId) throws Exception
	{
		if (logger.isDebugEnabled())
			logger.debug("getPost(" + postId + ")");

		Post post = persistenceManager.getPost(postId);
		if (securityManager.canCurrentUserReadPost(post))
			return post;
		else
			throw new Exception("The current user does not have permissions to read this post.");
	}

	public List<Post> getPosts(String placementId) throws Exception
	{
		// Get all the posts for the supplied site and filter them through the
		// security manager
		List<Post> filtered;
		List<Post> unfiltered = persistenceManager.getAllPost(placementId);
		filtered = securityManager.filter(unfiltered);
		return filtered;
	}

	public List<Post> getPosts(QueryBean query) throws Exception
	{
		// Get all the posts for the supplied site and filter them through the
		// security manager
		List<Post> filtered;
		List<Post> unfiltered = persistenceManager.getPosts(query);
		filtered = securityManager.filter(unfiltered);
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

	public Image getImage(String imageId, int mode) throws Exception
	{
		return persistenceManager.getImage(imageId, mode);
	}

	public File getFile(String fileId) throws Exception
	{
		// TODO Auto-generated method stub
		return persistenceManager.getFile(fileId);
	}

	public void addComment(Post post, Comment comment)
	{
		try
		{
			persistenceManager.addComment(comment);
			post.addComment(comment);

			if (persistenceManager.isUserNewCommentSubscriber(post.getCreatorId()))
			{
				String portalUrl = sakaiProxy.getPortalUrl();
				String siteId = sakaiProxy.getCurrentSiteId();
				String pageId = sakaiProxy.getCurrentPageId();
				String toolId = sakaiProxy.getCurrentToolId();
				
				String url = portalUrl
								+ "/site/" + siteId
								+ "/page/" + pageId
								+ "?toolstate-" + toolId + "=%2Fhome%3Fwicket%3AbookmarkablePage%3D%3Aorg.sakaiproject.blog.tool.pages.PostPage%26postId%3D"
								+ post.getId();
				
				sakaiProxy.sendEmailWithMessage(post.getCreatorId(), "New Blog Comment", sakaiProxy.getDisplayNameForTheUser(comment.getCreatorId()) + " commented on your post, titled '<a href=\"" + url + "\">" + post.getTitle() + "</a>'<br /><br />'<i>" + comment.getText() + "</i>'");
			}
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst addding comment", e);
		}
	}

	public void deleteComment(Post post, Comment comment)
	{
		try
		{
			persistenceManager.deleteComment(comment);
			post.removeComment(comment);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst deleting comment.", e);
		}
	}

	public void updateComment(Post post, Comment comment)
	{
		try
		{
			persistenceManager.updateComment(comment);
			// post.replaceComment(comment);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst updating comment.", e);
		}
	}

	public void removeResourceReference(String resourceId)
	{
		persistenceManager.removeResourceReference(resourceId);
	}

	public void replaceElement(Post post, PostElement element, int elementIndex)
	{
		try
		{
			persistenceManager.updatePostElement(post, element, elementIndex);
			post.replaceElement(element, elementIndex);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst replacing element", e);
		}
	}

	public void addElement(Post post, PostElement element, int elementIndex) throws Exception
	{
		try
		{
			post.addElement(element, elementIndex);
			persistenceManager.addPostElement(post, element);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst adding element", e);
			post.removeElement(elementIndex);
			throw e;
		}
	}

	public void addElement(Post post, PostElement element) throws Exception
	{
		try
		{
			post.addElement(element);
			persistenceManager.addPostElement(post, element);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst adding element", e);
			post.removeElement(element);
			throw e;
		}
	}

	public void setIndentation(Post post, PostElement element, int i)
	{
		try
		{
			// post.addElement(element, elementIndex);
			persistenceManager.setIndentation(post, element, i);
			element.setIndentation(i);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst adding element", e);

			// post.removeElement(elementIndex);
		}
	}

	public void moveUp(Post post, PostElement bottomElement)
	{
		try
		{
			int bottom = post.getElementPosition(bottomElement);
			int top = bottom - 1;
			persistenceManager.swapElements(post, top, bottom);
			PostElement topElement = post.getElement(top);
			post.replaceElement(bottomElement, top);
			post.replaceElement(topElement, bottom);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst moving element up", e);
		}
	}

	public void moveDown(Post post, PostElement topElement)
	{
		try
		{
			int top = post.getElementPosition(topElement);
			int bottom = top + 1;
			persistenceManager.swapElements(post, top, bottom);
			PostElement bottomElement = post.getElement(bottom);
			post.replaceElement(bottomElement, top);
			post.replaceElement(topElement, bottom);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst moving element down", e);
		}
	}

	public void deleteElement(Post post, int index)
	{
		try
		{
			persistenceManager.deleteElement(post, index);
			post.removeElement(index);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst deleting element", e);
		}
	}

	public void saveTitle(Post post)
	{
		try
		{
			persistenceManager.saveTitle(post);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst saving title", e);
		}
	}

	public void saveShortText(Post post)
	{
		try
		{
			persistenceManager.saveShortText(post);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst saving short text", e);
		}
	}

	public void saveReadOnly(Post post)
	{
		try
		{
			persistenceManager.saveReadOnly(post);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst saving read only", e);
		}
	}

	public void saveAllowComments(Post post)
	{
		try
		{
			persistenceManager.saveAllowComments(post);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst saving allow comments", e);
		}
	}

	public void saveVisibility(Post post)
	{
		try
		{
			Post currentPost = persistenceManager.getPost(post.getId());
			if (currentPost.isPrivate() && post.isReady())
			{
				Set<String> users = new TreeSet<String>();

				if (persistenceManager.getOptions().isLearningLogMode())
					users = sakaiProxy.getTutors();
				else
					users = persistenceManager.getNewPostEmailSubscribers();
				
				String portalUrl = sakaiProxy.getPortalUrl();
				String siteId = sakaiProxy.getCurrentSiteId();
				String pageId = sakaiProxy.getCurrentPageId();
				String toolId = sakaiProxy.getCurrentToolId();
				
				String url = portalUrl
								+ "/site/" + siteId
								+ "/page/" + pageId
								+ "?toolstate-" + toolId + "=%2Fhome%3Fwicket%3AbookmarkablePage%3D%3Aorg.sakaiproject.blog.tool.pages.PostPage%26postId%3D"
								+ post.getId();

				sakaiProxy.sendEmailWithMessage(users, "New Blog Post", sakaiProxy.getDisplayNameForTheUser(post.getCreatorId()) + " created a post, titled '<a href=\"" + url + "\">" + post.getTitle() + "</a>'<br /><br />" + post.getTitle() + "<br /><br /><i>" + post.getShortText() + "</i>");
			}

			persistenceManager.saveVisibility(post);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst saving visibility", e);
		}
	}

	public void recyclePost(String postId)
	{
		try
		{
			Post post = persistenceManager.getPost(postId);

			if (securityManager.canCurrentUserDeletePost(post))
			{
				try
				{
					persistenceManager.recyclePost(postId);
					post.setVisibility(State.RECYCLED);
				}
				catch (Exception e)
				{
					logger.error("The persistence manager threw an Exception whilst recycling post '" + postId + "'");
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Caught an exception whilst recycling post '" + postId + "'");
		}
	}

	public void setSakaiProxy(SakaiProxy sakaiProxy)
	{
		this.sakaiProxy = sakaiProxy;
	}

	public SakaiProxy getSakaiProxy()
	{
		return sakaiProxy;
	}

	public BlogSecurityManager getSecurityManager()
	{
		return securityManager;
	}

	public void setSecurityManager(BlogSecurityManager securityManager)
	{
		this.securityManager = securityManager;
	}

	public void setPersistenceManager(PersistenceManager pm)
	{
		this.persistenceManager = pm;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	private String serviceName()
	{
		return BlogManager.class.getName();
	}

	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		if (logger.isDebugEnabled())
			logger.debug("archive(siteId:" + siteId + ",archivePath:" + archivePath + ")");

		StringBuilder results = new StringBuilder();

		results.append(getLabel() + ": Started.\n");

		int postCount = 0;

		try
		{
			// start with an element with our very own (service) name
			Element element = doc.createElement(serviceName());
			element.setAttribute("version", "2.5.x");
			((Element) stack.peek()).appendChild(element);
			stack.push(element);

			Element blog = doc.createElement("blog");
			List<Post> posts = getPosts(siteId);
			if (posts != null && posts.size() > 0)
			{
				for (Post post : posts)
				{
					Element postElement = post.toXml(doc, stack);
					blog.appendChild(postElement);
					postCount++;
				}
			}

			((Element) stack.peek()).appendChild(blog);
			stack.push(blog);

			stack.pop();

			results.append(getLabel() + ": Finished. " + postCount + " post(s) archived.\n");
		}
		catch (Exception any)
		{
			results.append(getLabel() + ": exception caught. Message: " + any.getMessage());
			logger.warn(getLabel() + " exception caught. Message: " + any.getMessage());
		}

		stack.pop();

		return results.toString();
	}

	/**
	 * From EntityProducer
	 */
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport)
	{
		logger.debug("merge(siteId:" + siteId + ",root tagName:" + root.getTagName() + ",archivePath:" + archivePath + ",fromSiteId:" + fromSiteId);

		StringBuilder results = new StringBuilder();

		try
		{

			int postCount = 0;

			NodeList postNodes = root.getElementsByTagName(XmlDefs.POST);
			final int numberPosts = postNodes.getLength();

			for (int i = 0; i < numberPosts; i++)
			{
				Node child = postNodes.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE)
				{
					// Problem
					continue;
				}

				Element postElement = (Element) child;

				Post post = new Post();
				post.fromXml(postElement);
				post.setSiteId(siteId);

				savePost(post);
				postCount++;
			}

			results.append("Stored " + postCount + " posts.");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return results.toString();
	}

	/**
	 * From EntityProducer
	 */
	public Entity getEntity(Reference ref)
	{
		if (logger.isDebugEnabled())
			logger.debug("getEntity(Ref ID:" + ref.getId() + ")");

		Entity rv = null;

		try
		{
			String reference = ref.getReference();

			int lastIndex = reference.lastIndexOf(Entity.SEPARATOR);
			String postId = reference.substring(lastIndex, reference.length() - lastIndex);
			rv = getPost(postId);
		}
		catch (Exception e)
		{
			logger.warn("getEntity(): " + e);
		}

		return rv;
	}

	/**
	 * From EntityProducer
	 */
	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		if (logger.isDebugEnabled())
			logger.debug("getEntityAuthzGroups(Ref ID:" + ref.getId() + "," + userId + ")");

		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityDescription(Reference arg0)
	{
		return null;
	}

	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		try
		{
			String reference = ref.getReference();

			int lastIndex = reference.lastIndexOf(Entity.SEPARATOR);
			String postId = reference.substring(lastIndex, reference.length() - lastIndex);
			Entity entity = getPost(postId);
			return entity.getProperties();
		}
		catch (Exception e)
		{
			logger.warn("getEntity(): " + e);
			return null;
		}
	}

	/**
	 * From EntityProducer
	 */
	public String getEntityUrl(Reference ref)
	{
		return getEntity(ref).getUrl();
	}

	/**
	 * From EntityProducer
	 */
	public HttpAccess getHttpAccess()
	{
		return new HttpAccess()
		{
			public void handleAccess(HttpServletRequest arg0, HttpServletResponse arg1, Reference arg2, Collection arg3) throws EntityPermissionException, EntityNotDefinedException, EntityAccessOverloadException, EntityCopyrightException
			{
				try
				{
					String referenceString = arg2.getReference();
					String url = "http://btc224000006.lancs.ac.uk/portal/tool/53eb45d4-7dba-4e62-be46-0fc952f41bc4/home?wicket:bookmarkablePage=%3Aorg.sakaiproject.blog.tool.pages.PostPage&postId=";
					url += referenceString.substring(referenceString.lastIndexOf(Entity.SEPARATOR) + 1);
					logger.debug("URL:" + url);
					arg1.sendRedirect(url);
					// arg1.sendRedirect("http://btc224000006.lancs.ac.uk/portal/tool/fe1ba6dd-c67f-48b9-a4d4-9208ced4be9e/home?wicket:bookmarkablePage=%3Aorg.sakaiproject.blog.tool.pages.PostPage&postId=f91f06d9-cec3-4f51-9ab3-6090d8523150");
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}

	/**
	 * From EntityProducer
	 */
	public String getLabel()
	{
		// TODO Auto-generated method stub
		return "blog";
	}

	/**
	 * From EntityProducer
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		if (!reference.startsWith(BlogManager.REFERENCE_ROOT))
			return false;

		return true;
	}

	public boolean willArchiveMerge()
	{
		return true;
	}

	public String getEntityPrefix()
	{
		return BlogManager.ENTITY_PREFIX;
	}

	public boolean entityExists(String id)
	{
		String postId = id.substring(id.lastIndexOf(Entity.SEPARATOR));

		try
		{
			if (persistenceManager.postExists(postId))
				return true;
		}
		catch (Exception e)
		{
			logger.error("entityExists threw an exception", e);
		}

		return false;
	}

}
