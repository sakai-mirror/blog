
package org.sakaiproject.blog.impl;

import java.util.*;

import org.apache.log4j.Logger;
import org.sakaiproject.blog.api.BlogFunctions;
import org.sakaiproject.blog.api.BlogManager;
import org.sakaiproject.blog.api.BlogSecurityManager;
import org.sakaiproject.blog.api.PersistenceManager;
import org.sakaiproject.blog.api.QueryBean;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blog.api.XmlDefs;
import org.sakaiproject.blog.api.datamodel.Comment;
import org.sakaiproject.blog.api.datamodel.File;
import org.sakaiproject.blog.api.datamodel.Image;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.datamodel.PostElement;
import org.sakaiproject.blog.api.datamodel.State;
import org.sakaiproject.entity.api.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BlogManagerImpl implements BlogManager//, EntityProducer
{
    private Logger logger = Logger.getLogger(BlogManagerImpl.class);

    private PersistenceManager persistenceManager;

    private BlogSecurityManager securityManager;

    private SakaiProxy sakaiProxy;

    public void init()
    {
        if(logger.isDebugEnabled()) logger.debug("init()");

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
        if(logger.isDebugEnabled()) logger.debug("getPost(" + postId + ")");

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
        post.addComment(comment);
        persistenceManager.addComment(comment);
        
        try
        {
        	if(persistenceManager.isUserNewCommentSubscriber(post.getCreatorId()))
        	{
        		sakaiProxy.sendEmailWithMessage(post.getCreatorId()
        											,"New Blog Comment",
        											sakaiProxy.getDisplayNameForTheUser(comment.getCreatorId()) + " commented on your post, titled '<a href=\"" + post.getUrl() + "\">" + post.getTitle() + "</a>'<br /><br />'<i>" + comment.getText() + "</i>'");
        	}
        }
        catch(Exception e)
        {
        }
    }

    public void deleteComment(Post post, Comment comment)
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
            persistenceManager.updatePostElement(post, element, elementIndex);
            post.replaceElement(element, elementIndex);
        }
        catch (Exception e)
        {
            logger.error("Caught exception whilst replacing element", e);
        }
    }

    public void addElement(Post post, PostElement element, int elementIndex)
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
        }
    }

    public void addElement(Post post, PostElement element)
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
        }
    }

    public void setIndentation(Post post, PostElement element, int i)
    {
        try
        {
            //post.addElement(element, elementIndex);
            persistenceManager.setIndentation(post, element, i);
            element.setIndentation(i);
        }
        catch (Exception e)
        {
            logger.error("Caught exception whilst adding element", e);

            //post.removeElement(elementIndex);
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
        	if(currentPost.isPrivate() && post.isReady())
        	{
        		Set<String> users = persistenceManager.getNewPostEmailSubscribers();
        		sakaiProxy.sendEmailWithMessage(users,"New Blog Post",sakaiProxy.getDisplayNameForTheUser(post.getCreatorId()) + " created a post, titled '<a href=\"" + post.getUrl() + "\">" + post.getTitle() + "</a>'<br /><br />" + post.getTitle() + "<br /><br /><i>" + post.getShortText() + "</i>");
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
                persistenceManager.recyclePost(postId);
                post.setVisibility(State.RECYCLED);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
        if(logger.isDebugEnabled()) logger.debug("archive(siteId:" + siteId + ",archivePath:" + archivePath +")");

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
        logger.debug("merge(siteId:" + siteId + ",root tagName:" + root.getTagName() + ",archivePath:" + archivePath + ",fromSiteId:" + fromSiteId);

        StringBuilder results = new StringBuilder();

        try
        {

            int postCount = 0;

            NodeList postNodes = root.getElementsByTagName(XmlDefs.POST);
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
                post.fromXml(postElement);
                post.setSiteId(siteId);

                savePost(post);
                postCount++;
            }

            results.append("Stored " + postCount + " posts.");

        }
        catch(Exception e)
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
        if(logger.isDebugEnabled()) logger.debug("getEntity(Ref ID:" + ref.getId() + ")");

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
        if(logger.isDebugEnabled()) logger.debug("getEntityAuthzGroups(Ref ID:" + ref.getId() + "," + userId + ")");

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

    public boolean parseEntityReference(String reference, Reference ref)
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean willArchiveMerge()
    {
        return true;
    }

}