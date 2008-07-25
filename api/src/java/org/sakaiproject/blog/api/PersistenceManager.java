package org.sakaiproject.blog.api;

import java.util.List;
import java.util.Set;

import org.sakaiproject.blog.api.datamodel.BlogOptions;
import org.sakaiproject.blog.api.datamodel.Comment;
import org.sakaiproject.blog.api.datamodel.File;
import org.sakaiproject.blog.api.datamodel.Image;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.datamodel.PostElement;
import org.sakaiproject.blog.api.datamodel.Preferences;

public interface PersistenceManager
{
	public void setupTables() throws Exception;

	public boolean existPost(String OID) throws Exception;

	public List<Post> getAllPost(String placementId) throws Exception;

	public List<Post> getAllPost(String placementId, boolean populate) throws Exception;

	public void addComment(Comment comment);

	public void deleteComment(Comment comment);

	public void createPost(Post post) throws Exception;

	public void savePost(Post post) throws Exception;

	public void deletePost(String postId) throws Exception;

	public void recyclePost(String postId) throws Exception;

	public List<Post> getPosts(QueryBean query) throws Exception;

	public Post getPost(String postId) throws Exception;

	public Post getPost(String postId, boolean fully) throws Exception;

	public Image getImage(String imageId, int mode) throws Exception;

	public File getFile(String fileId) throws Exception;

	public void setSakaiProxy(SakaiProxy sakaiProxy);

	public SakaiProxy getSakaiProxy();

	public void setSecurityManager(BlogSecurityManager securityManager);

	public BlogSecurityManager getSecurityManager();

	public List<BlogMember> getPublicBloggers();

	public void updateComment(Comment comment);

	public void removeResourceReference(String resourceId);

	public void updatePostElement(Post post, PostElement element, int elementIndex) throws Exception;

	public void addPostElement(Post post, PostElement element) throws Exception;

	public void setIndentation(Post post, PostElement element, int i) throws Exception;

	public void swapElements(Post post, int first, int second) throws Exception;

	public void deleteElement(Post post, int position) throws Exception;

	public void saveTitle(Post post) throws Exception;

	public void saveShortText(Post post) throws Exception;

	public void saveReadOnly(Post post) throws Exception;

	public void saveAllowComments(Post post) throws Exception;

	public void saveVisibility(Post post) throws Exception;

	public BlogOptions getOptions();

	public void saveOptions(BlogOptions options) throws Exception;

	public Preferences getPreferences();

	public void savePreferences(Preferences preferences) throws Exception;

	public Set<String> getNewPostEmailSubscribers() throws Exception;

	public boolean isUserNewCommentSubscriber(String userId) throws Exception;
}
