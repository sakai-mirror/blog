package org.sakaiproject.tool.blog.entity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.tool.blog.api.SakaiProxy;
import org.sakaiproject.tool.blog.api.datamodel.Post;
import org.sakaiproject.tool.blog.impl.managers.PostManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BlogEntityProducer implements EntityProducer
{
	private Logger logger = Logger.getLogger(BlogEntityProducer.class);
	
	private SakaiProxy sakaiProxy;
	
	private PostManager postManager;
	
	private String serviceName()
	{
		return BlogEntityProducer.class.getName();
	}

	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		StringBuilder results = new StringBuilder();
		
		results.append(getLabel() + ": Started.\n");
		
		int postCount = 0;
		
		try
	      {
	         // start with an element with our very own (service) name         
	         Element element = doc.createElement(serviceName());
	         element.setAttribute("version", "1.0");
	         ((Element) stack.peek()).appendChild(element);
	         stack.push(element);

	         Element blog = doc.createElement("blog");
	         List<Post> posts = postManager.getPosts(siteId);
	         if (posts != null && posts.size() > 0)
	         {
	            for(Post post : posts)
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
	
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport)
	{
		 StringBuilder results = new StringBuilder();
		 
		 try
		 {
			 
			 int postCount = 0;
			 
			 NodeList postNodes = root.getChildNodes();
			 final int numberPosts = postNodes.getLength();
			 
			 for(int i = 0;i < numberPosts;i++)
			 {
				 Node child = postNodes.item(i);
				 if(child.getNodeType() != Node.ELEMENT_NODE)
				 {
					 // Problem
					 continue;
				 }
				 
				 Element postElement = (Element) child;
				 
				 Post post = new Post();
				 post.setSiteId(siteId);
				 post.fromXml(postElement);
				 
				 postManager.savePost(post);
				 postCount++;
			 }
			 
			 results.append("Stored " + postCount + " posts.");
			 
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		
		return null;
	}

	public Entity getEntity(Reference ref)
	{
	      Entity rv = null;

	      try
	      {
	            rv = postManager.getPost(ref.getReference());
	      }
	      catch (Exception e)
	      {
	         logger.warn("getEntity(): " + e);
	      }

	      return rv;
	}

	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityDescription(Reference arg0)
	{
		return null;
	}

	public ResourceProperties getEntityResourceProperties(Reference arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityUrl(Reference ref)
	{
		return getEntity(ref).getUrl();
	}

	public HttpAccess getHttpAccess()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel()
	{
		// TODO Auto-generated method stub
		return "blog";
	}

	public boolean parseEntityReference(String arg0, Reference arg1)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean willArchiveMerge()
	{
		// TODO Auto-generated method stub
		return true;
	}

	public void setSakaiProxy(SakaiProxy sakaiProxy)
	{
		this.sakaiProxy = sakaiProxy;
	}

	public void setPostManager(PostManager postManager)
	{
		this.postManager = postManager;
	}
}
