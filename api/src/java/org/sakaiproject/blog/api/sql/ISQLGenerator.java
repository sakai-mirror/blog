package org.sakaiproject.blog.api.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;

import org.sakaiproject.blog.api.QueryBean;
import org.sakaiproject.blog.api.datamodel.BlogOptions;
import org.sakaiproject.blog.api.datamodel.Comment;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.datamodel.PostElement;
import org.sakaiproject.blog.api.datamodel.Preferences;

public interface ISQLGenerator
{
	public final static String APOSTROFE = "&&-apos-s-&k";

	public static final String DEFAULT_PREFIX = "BLOG_";

	public static final String TABLE_POST = DEFAULT_PREFIX + "POST";
	public static final String TABLE_IMAGE = DEFAULT_PREFIX + "IMAGE";
	public static final String TABLE_COMMENT = DEFAULT_PREFIX + "COMMENT";
	public static final String TABLE_FILE = DEFAULT_PREFIX + "FILE";
	public static final String TABLE_PARAGRAPH = DEFAULT_PREFIX + "PARAGRAPH";
	public static final String TABLE_POST_ELEMENT = DEFAULT_PREFIX + "POST_ELEMENT";
	public static final String TABLE_LINK = DEFAULT_PREFIX + "LINK";
	public static final String TABLE_OPTIONS = DEFAULT_PREFIX + "OPTIONS";
	public static final String TABLE_PREFERENCES = DEFAULT_PREFIX + "PREFERENCES";

	public static final String POST_ID = "POST_ID";

	// From BLOGGER_POST
	public static final String TITLE = "TITLE";

	public static final String CREATED_DATE = "CREATED_DATE";
	public static final String MODIFIED_DATE = "MODIFIED_DATE";

	// From BLOGGER_POST
	public static final String VISIBILITY = "VISIBILITY";

	public static final String USER_ID = "USER_ID";
	public static final String CREATOR_ID = "CREATOR_ID";
	
	public static final String NEW_POST_ALERT = "NEW_POST_ALERT";
	
	public static final String NEW_COMMENT_ALERT = "NEW_COMMENT_ALERT";

	// From BLOGGER_POST
	public static final String SITE_ID = "SITE_ID";

	public static final String FULL_RESOURCE_ID = "FULL_RESOURCE_ID";
	public static final String WEB_RESOURCE_ID = "WEB_RESOURCE_ID";
	
	public static final String IMAGE_ID = "IMAGE_ID";
	public static final String LINK_ID = "LINK_ID";
	public static final String PARAGRAPH_ID = "PARAGRAPH_ID";
	public static final String ELEMENT_ID = "ELEMENT_ID";
	public static final String ELEMENT_TYPE = "ELEMENT_TYPE";
	public static final String POSITION = "POSITION";
	public static final String MODE = "MODE";
	
	public static final String TIMEOUT = "TIMEOUT";
	
	public static final String URL = "URL";
	public static final String DISPLAY_NAME = "DISPLAY_NAME";
	public static final String NAME = "NAME";
	
	public static final String INDENTATION = "INDENTATION";

	public static final String FILE_ID = "FILE_ID";
	public static final String FILE_NAME = "FILE_NAME";
	
	public static final String RESOURCE_ID = "RESOURCE_ID";
	
	public static final String MIME_TYPE = "MIME_TYPE";
	
	public static final String LENGTH = "LENGTH";

	// From BLOGGER_POST
	public static final String SHORT_TEXT = "SHORT_TEXT";
	
	// From BLOGGER_POST
	public static final String KEYWORDS = "KEYWORDS";
	
	// From BLOGGER_POST
	public static final String ALLOW_COMMENTS = "ALLOW_COMMENTS";
	
	// From BLOGGER_POST
	public static final String READ_ONLY = "READ_ONLY";

	// From BLOGGER_COMMENT
	public static final String COMMENT_ID = "COMMENT_ID";

	// From BLOGGER_COMMENT
	public static final String CONTENT = "CONTENT";

	public abstract List<String> getCreateStatementsForPost();

	public abstract List<String> getSelectStatementsForQuery(QueryBean query);

	public abstract String getSelectAllPost(String siteId);

	public abstract String getSelectPost(String OID);
	
	public String getSelectComments(String postId);

	public abstract String getSelectImage(String imageId);

	public abstract String getSelectFile(String fileId);

	public abstract PreparedStatement getInsertStatementForPost(Post post, Connection connection) throws Exception;
	
	public abstract PreparedStatement getInsertStatementForComment(Comment comment,Connection connection) throws Exception;
	
	public abstract String getSelectIdImagesFromPost(Post post);
	public abstract String getSelectIdFilesFromPost(Post post);
	
	/**
	 * @param postId
	 * @return
	 */
	public abstract List<String> getDeleteStatementsForPost(String postId,Connection connection);

	//public abstract String getDeleteStatementForImage(String imageId);
	//public abstract String getDeleteStatementForFile(String idFile);

	public abstract String getSelectImages(String postId);

	public abstract String getSelectFiles(String postId);

	public abstract String getSelectPostElements(String postId);

	public abstract String getSelectParagraphs(String postId);

	public abstract String getSelectLinks(String postId);

	public abstract String getSelectParagraph(String paragraphId);

	public abstract String getSelectLink(String linkId);

	public abstract String getSelectPublicBloggers();

	public abstract String getCountPublicPosts(String userId);

	public abstract String getDeleteStatementForComment(Comment comment, Connection connection);

	public abstract String getUpdateStatementForComment(Comment comment, Connection connection);

	public abstract Collection getRemoveResourceStatements(String resourceId, Connection connection) throws Exception;

	public abstract Collection getUpdateElementStatements(Post post, PostElement element, int elementIndex, Connection connection) throws Exception;

	public abstract Collection getAddElementStatements(Post post, PostElement element, Connection connection) throws Exception;

	public abstract String getSetIndentationStatement(Post post, PostElement element, int i, Connection connection);

	public abstract Collection getSwapElementsStatements(Post post, int first, int second, Connection connection) throws Exception;

	public abstract Collection getDeleteStatementsForElement(Post post, int position,Connection connection) throws Exception;

	public abstract String getSaveTitleStatement(Post post);

	public abstract String getSaveShortTextStatement(Post post);

	public abstract String getSaveReadOnlyStatement(Post post);

	public abstract String getSaveAllowCommentsStatement(Post post);

	public abstract String getSaveVisibilityStatement(Post post);

	public abstract String getSaveOptionsStatement(BlogOptions options,Connection connection) throws Exception;

	public abstract String getSelectOptionsStatement(String placementId);
	
	public abstract List<String> getInsertStatementsForPostElement(Post post, Connection connection) throws Exception;

	public abstract String getRecycleStatementForPost(String postId, Connection connection);

	public abstract String getSelectPreferencesStatement(String userId,String placementId);

	public abstract String getSavePreferencesStatement(Preferences preferences, Connection connection) throws Exception;

	public abstract String getSelectNewPostEmailUnSubscribersStatement(String currentSiteId);

	public abstract String getSelectNewCommentUnSubscribersStatement(String currentSiteId);
}