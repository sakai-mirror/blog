package org.sakaiproject.blog.api;

import java.util.*;

import org.sakaiproject.blog.api.datamodel.Comment;
import org.sakaiproject.blog.api.datamodel.File;
import org.sakaiproject.blog.api.datamodel.Image;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.datamodel.PostElement;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;

public interface BlogManager extends EntityProducer
{
	public static final String ENTITY_PREFIX = "blog";
	public static final String REFERENCE_ROOT = Entity.SEPARATOR + ENTITY_PREFIX;
	
    public Post getPost(String postId) throws Exception;

    public List<Post> getPosts(String placementId) throws Exception;

    public List<Post> getPosts(QueryBean query) throws Exception;

    public void createPost(Post post) throws Exception;

    public void savePost(Post post) throws Exception;

    public void deletePost(String postId);

    public Image getImage(String imageId, int mode) throws Exception;

    public File getFile(String fileId) throws Exception;

    public void addComment(Post post, Comment comment);

    public void deleteComment(Post post, Comment comment);

    public void updateComment(Post post, Comment comment);

    public void removeResourceReference(String resourceId);

    public void replaceElement(Post post, PostElement element, int elementIndex);

    public void addElement(Post post, PostElement element, int elementIndex) throws Exception;

    public void addElement(Post post, PostElement element) throws Exception;

    public void setIndentation(Post post, PostElement element, int i);

    public void moveUp(Post post, PostElement bottomElement);

    public void moveDown(Post post, PostElement topElement);

    public void deleteElement(Post post, int index);

    public void saveTitle(Post post);

    public void saveShortText(Post post);

    public void saveReadOnly(Post post);

    public void saveAllowComments(Post post);

    public void saveVisibility(Post post);

    public void recyclePost(String postId);

    public SakaiProxy getSakaiProxy();

    public BlogSecurityManager getSecurityManager();

    public PersistenceManager getPersistenceManager();
}