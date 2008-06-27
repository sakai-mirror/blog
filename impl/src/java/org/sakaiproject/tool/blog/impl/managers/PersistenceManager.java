package org.sakaiproject.tool.blog.impl.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.sakaiproject.tool.blog.api.BlogMember;
import org.sakaiproject.tool.blog.api.QueryBean;
import org.sakaiproject.tool.blog.api.SakaiProxy;
import org.sakaiproject.tool.blog.api.datamodel.BlogOptions;
import org.sakaiproject.tool.blog.api.datamodel.Comment;
import org.sakaiproject.tool.blog.api.datamodel.File;
import org.sakaiproject.tool.blog.api.datamodel.Image;
import org.sakaiproject.tool.blog.api.datamodel.LinkRule;
import org.sakaiproject.tool.blog.api.datamodel.Paragraph;
import org.sakaiproject.tool.blog.api.datamodel.Post;
import org.sakaiproject.tool.blog.api.datamodel.PostElement;
import org.sakaiproject.tool.blog.api.datamodel.State;
import org.sakaiproject.tool.blog.impl.sql.HiperSonicGenerator;
import org.sakaiproject.tool.blog.impl.sql.ISQLGenerator;
import org.sakaiproject.tool.blog.impl.sql.MySQLGenerator;
import org.sakaiproject.tool.blog.impl.sql.OracleSQLGenerator;

public class PersistenceManager
{
	private Logger logger = Logger.getLogger(PersistenceManager.class);

	private ISQLGenerator sqlGenerator;

	private SakaiProxy sakaiProxy;

	private BlogSecurityManager securityManager;

	public PersistenceManager()
	{
		if (logger.isDebugEnabled())
			logger.debug("PersistenceManager()");
	}
	
	public void init() throws PersistenceException
	{
		init(true);
	}

	public void init(boolean create) throws PersistenceException
	{
		if (logger.isDebugEnabled())
			logger.debug("PersistenceManager.init()");

		String vendor = sakaiProxy.getVendor();

		// TODO load the proper class using reflection. We can use a named based system to locate the correct SQLGenerator
		if (vendor.equals("mysql"))
			sqlGenerator = new MySQLGenerator();
		else if (vendor.equals("oracle"))
			sqlGenerator = new OracleSQLGenerator();
		else if (vendor.equals("hsqldb"))
			sqlGenerator = new HiperSonicGenerator();
		else
			throw new PersistenceException("Unknown database vendor:" + vendor);

		if (create && sakaiProxy.isAutoDDL())
			setupTables();
	}

	public void setupTables() throws PersistenceException
	{
		if (logger.isDebugEnabled())
			logger.debug("setupTables()");
		
		Connection connection = null;

		try
		{
			connection = getConnection();
			boolean oldAutoCommitFlag = connection.getAutoCommit();
			connection.setAutoCommit(false);
			
			try
			{
				List<String> statements = sqlGenerator.getCreateStatementsForPost();
				
				Statement statement = connection.createStatement();

				for (String sql : statements)
					statement.executeUpdate(sql);

				connection.commit();
			}
			catch(SQLException sqle)
			{
				logger.error("Caught exception whilst setting up tables. Rolling back ...",sqle);
				connection.rollback();
			}
			finally
			{
				connection.setAutoCommit(oldAutoCommitFlag);
			}
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst setting up tables", e);
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public boolean existPost(String OID) throws PersistenceException
	{
		if (logger.isDebugEnabled())
			logger.debug("existPost(" + OID + ")");

		Connection connection = getConnection();
		
		try
		{
			ResultSet rs = executeQuerySQL(sqlGenerator.getSelectPost(OID), connection);
			return (rs.next());
		}
		catch (SQLException e)
		{
			throw new PersistenceException(e);
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public List<Post> getAllPost(String placementId) throws PersistenceException
	{
		return getAllPost(placementId, false);
	}

	public List<Post> getAllPost(String placementId, boolean populate) throws PersistenceException
	{
		if (logger.isDebugEnabled())
			logger.debug("getAllPost(" + placementId + ")");

		List<Post> result = new ArrayList<Post>();

		Connection connection = getConnection();
		try
		{
			ResultSet rs = executeQuerySQL(sqlGenerator.getSelectAllPost(placementId), connection);
			result = transformResultSetInPostCollection(rs, connection, populate);
			rs.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			releaseConnection(connection);
		}

		return result;
	}

	public void addComment(Comment comment)
	{
		Connection connection = null;
		
		try
		{
			connection = getConnection();
			PreparedStatement sql = sqlGenerator.getInsertStatementForComment(comment, connection);
			sql.executeUpdate();
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst adding comment.", e);
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public void deleteComment(Comment comment)
	{
		Connection connection = null;
		
		try
		{
			connection = getConnection();
			String sql = sqlGenerator.getDeleteStatementForComment(comment, connection);
			executeSQL(sql, connection);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst deleting comment.", e);
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public void createPost(Post post) throws PersistenceException
	{
		if (logger.isDebugEnabled()) logger.debug("createPost()");

		if (!post.isDirty()) return;

		Connection connection = getConnection();
			
		try
		{
			PreparedStatement statement = sqlGenerator.getInsertStatementForPost(post, connection);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst inserting post.",e);
		}
		finally
		{
			releaseConnection(connection);
		}
	}
	
	public void savePost(Post post) throws PersistenceException
	{
		if (logger.isDebugEnabled()) logger.debug("savePost()");

		if (!post.isDirty()) return;

		Connection connection = getConnection();
			
		try
		{
			Iterator<PostElement> elements = post.getElements();
			while(elements.hasNext())
			{
				PostElement e = elements.next();
				Collection statements = sqlGenerator.getAddElementStatements(post,e,connection);
				executeSQL(statements, connection);
			}
			
			PreparedStatement statement = sqlGenerator.getInsertStatementForPost(post, connection);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst inserting post.",e);
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	void synchroniseImagesAndFiles(Post oldModule, Post module, Connection connection) throws Exception
	{
		List<String> successfulResourceIds = new ArrayList<String>();

		try
		{
			// If post has moved from anything to PUBLIC we need to move the resources
			if (!oldModule.isPublic() && module.isPublic())
				moveResourcesToPublic(module, successfulResourceIds);
			else
			{

				List<Image> newImages = module.getImages();
				for (Image newImage : newImages)
				{
					if (oldModule.hasImage(newImage) == false)
						storeImage(module,newImage,successfulResourceIds);
				}

				List<Image> oldImages = oldModule.getImages();
				for (Image oldImage : oldImages)
				{
					if (!module.hasImage(oldImage))
						removeImage(oldImage);
				}

				for (File newFile : module.getFiles())
				{
					if (!oldModule.hasFile(newFile.getId()))
						storeFile(module,newFile,successfulResourceIds);
				}

				for (File oldFile : oldModule.getFiles())
				{
					if (!module.hasFile(oldFile.getId()))
						removeFile(oldFile);
				}
			}
		}
		catch (Exception e)
		{
			sakaiProxy.deleteResources(successfulResourceIds.toArray(new String[0]));
			throw e;
		}
	}

	private void moveResourcesToPublic(Post module, List<String> successfulResourceIds) throws Exception
	{
		for (Image image : module.getImages())
		{
			removeImage(image);
			storeImage(module,image,successfulResourceIds);
		}

		for (File file : module.getFiles())
		{
			removeFile(file);
			storeFile(module,file,successfulResourceIds);
		}
	}

	void removeImage(Image image)
	{
		String fullResourceId = image.getFullResourceId();
		String webResourceId = image.getWebResourceId();

		sakaiProxy.deleteResources(new String[] { fullResourceId, webResourceId });
	}

	void removeFile(File file)
	{
		String resourceId = file.getResourceId();

		sakaiProxy.deleteResources(new String[] { resourceId });
	}

	void storeImage(Post post,Image image,List<String> successfulResourceIds) throws Exception
	{
		String fileName = image.getFileName();
		String extension = fileName.substring(fileName.lastIndexOf("."));

		String full = fileName.substring(0, fileName.lastIndexOf(".")) + "_full" + extension;
		String web = fileName.substring(0, fileName.lastIndexOf(".")) + "_web" + extension;

		String fullResourceId = sakaiProxy.saveFile(post,full,"image/jpeg",image.getFullContent());
		if (fullResourceId == null)
		{
			// sakaiProxy.deleteResources(successfulResourceIds);
			throw new Exception("saveFile returned null. The file was not saved.");
		}
		successfulResourceIds.add(fullResourceId);

		String webResourceId = sakaiProxy.saveFile(post,web, "image/jpeg", image.getImageContentWithWebSize());
		if (webResourceId == null)
		{
			// sakaiProxy.deleteResources(successfulResourceIds);
			throw new Exception("saveFile returned null. The file was not saved.");
		}
		successfulResourceIds.add(webResourceId);

		image.setFullResourceId(fullResourceId);
		image.setWebResourceId(webResourceId);
	}

	void storeFile(Post post,File file,List<String> successfulResourceIds) throws Exception
	{
		String resourceId = sakaiProxy.saveFile(post,file.getFileName(), file.getMimeType(),file.getContent());
		if (resourceId == null)
		{
			// sakaiProxy.deleteResources(successfulResourceIds.toArray(new String[0]));
			throw new Exception("saveFile returned null. The file was not saved.");
		}

		successfulResourceIds.add(resourceId);

		file.setResourceId(resourceId);
	}

	public void deletePost(String postId) throws PersistenceException
	{
		if (logger.isDebugEnabled())
			logger.debug("deletePost(" + postId + ")");
		
		Connection connection = getConnection();
		boolean oldAutoCommitFlag = true;
		try
		{
			List<String> sqlStatements = sqlGenerator.getDeleteStatementsForPost(postId, connection);

			oldAutoCommitFlag = connection.getAutoCommit();

			connection.setAutoCommit(false);

			executeSQL(sqlStatements, connection);
			
			sakaiProxy.deleteFolderForPost(postId);
			
			connection.commit();
		}
		catch (SQLException e)
		{
			logger.error("Caught exception whilst deleting post. Rolling back ...",e);
			try
			{
				connection.rollback();
			}
			catch (SQLException e1)
			{
				logger.error("Caught exception whilst rolling back transaction",e);
			}
		}
		finally
		{
			try
			{
				connection.setAutoCommit(oldAutoCommitFlag);
			}
			catch (SQLException e)
			{
				logger.error("Caught exception whilst resetting connection's autocommit flag",e);
			}

			releaseConnection(connection);
		}
	}
	
	public void recyclePost(String postId) throws PersistenceException
	{
		if (logger.isDebugEnabled())
			logger.debug("recyclePost(" + postId + ")");

		Connection connection = getConnection();
		try
		{
			String sql
				= sqlGenerator.getRecycleStatementForPost(postId, connection);

			executeSQL(sql, connection);
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	private ResultSet executeQuerySQL(String sql, Connection connection) throws PersistenceException
	{
		if (logger.isDebugEnabled())
			logger.debug("executeQuerySQL(" + sql + "," + connection + ")");

		try
		{
			Statement statement = connection.createStatement();
			return statement.executeQuery(sql);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new PersistenceException(e.getMessage());
		}
	}

	private void executeSQL(String sql, Connection connection) throws PersistenceException
	{
		if (logger.isDebugEnabled())
			logger.debug("executeSQL(" + sql + "," + connection + ")");

		Collection sqlList = new ArrayList();
		sqlList.add(sql);
		executeSQL(sqlList, connection);
	}

	public List<Post> getPosts(QueryBean query) throws PersistenceException
	{
		// if (logger.isDebugEnabled())
		// logger.debug("getPost(" + postId + ")");
		
		List<Post> posts = new ArrayList<Post>();

		Connection connection = getConnection();
		try
		{
			List<String> statements = sqlGenerator.getSelectStatementsForQuery(query);
			for(String statement : statements)
			{
				ResultSet rs = executeQuerySQL(statement, connection);
				posts.addAll(transformResultSetInPostCollection(rs, connection, false));
				
				try
				{
					rs.close();
				}
				catch(SQLException sqle)
				{
					logger.warn("Caught exception whilst closing record set",sqle);
				}
			}
			/*
			String statement = sqlGenerator.getSelectStatementForQuery(query);
			
			ResultSet rs = executeQuerySQL(statement, connection);
			posts.addAll(transformResultSetInPostCollection(rs, connection, false));
			
			try
			{
				rs.close();
			}
			catch(SQLException sqle)
			{
				logger.warn("Caught exception whilst closing record set",sqle);
			}
			*/
		}
		finally
		{
			releaseConnection(connection);
		}
		
		return posts;
	}

	public Post getPost(String postId) throws PersistenceException
	{
		return getPost(postId, false);
	}

	public Post getPost(String postId, boolean fully) throws PersistenceException
	{
		if (logger.isDebugEnabled())
			logger.debug("getPost(" + postId + ")");

		Connection connection = getConnection();
		try
		{
			String statement = sqlGenerator.getSelectPost(postId);
			ResultSet rs = executeQuerySQL(statement, connection);
			List<Post> posts = transformResultSetInPostCollection(rs, connection, fully);

			if (posts.size() == 0)
				throw new PersistenceException("getPost: Unable to find post with id:" + postId);
			if (posts.size() > 1)
				throw new PersistenceException("getPost: there are more than one post with id:" + postId);

			return posts.get(0);
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	private void executeSQL(Collection sql, Connection connection) throws PersistenceException
	{
		if (logger.isDebugEnabled())
			logger.debug("executeSQL(" + sql + "," + connection + ")");

		try
		{
			Iterator it = sql.iterator();
			while (it.hasNext())
			{
				Object sentence = it.next();
				if (sentence instanceof String)
				{
					String sqlSentence = (String) sentence;
					Statement statement = connection.createStatement();
					if (sqlSentence.indexOf("SELECT") == 0)
						statement.executeQuery(sqlSentence);
					else
						statement.executeUpdate(sqlSentence);
				}
				else if (sentence instanceof PreparedStatement)
				{
					try
					{
						// we use prepared statements to insert or update data with BLOB
						((PreparedStatement) sentence).executeUpdate();
					}
					catch (SQLException e)
					{
						logger.error("Caught exception whilst executing prepared statement", e);
					}
				}
			}
		}
		catch (SQLException e)
		{
			logger.error("Caught exception whilst executing SQL", e);

			throw new PersistenceException(e.getMessage());
		}
	}

	private List<Post> transformResultSetInPostCollection(ResultSet rs, Connection connection, boolean populate) throws PersistenceException
	{
		List<Post> result = new ArrayList<Post>();
		if (rs == null)
			return result;
		try
		{
			while (rs.next())
			{
				Post post = new Post();

				String postId = rs.getString(ISQLGenerator.POST_ID);
				post.setId(postId);

				String title = rs.getString(ISQLGenerator.TITLE);
				post.setTitle(title);

				Date postCreatedDate = rs.getTimestamp(ISQLGenerator.CREATED_DATE);
				post.setCreatedDate(postCreatedDate);

				Date postModifiedDate = rs.getTimestamp(ISQLGenerator.MODIFIED_DATE);
				post.setModifiedDate(postModifiedDate);

				String postCreatorId = rs.getString(ISQLGenerator.CREATOR_ID);
				post.setCreatorId(postCreatorId);

				String siteId = rs.getString(ISQLGenerator.SITE_ID);
				post.setSiteId(siteId);

				String visibility = rs.getString(ISQLGenerator.VISIBILITY);
				post.setVisibility(visibility);

				String shortText = rs.getString(ISQLGenerator.SHORT_TEXT);
				post.setShortText(shortText);

				String keywords = rs.getString(ISQLGenerator.KEYWORDS);
				post.setKeywords(keywords);

				int readOnly = rs.getInt(ISQLGenerator.READ_ONLY);
				post.setReadOnly(readOnly == 1);

				int allowComments = rs.getInt(ISQLGenerator.ALLOW_COMMENTS);
				post.setCommentable(allowComments == 1);

				String statement = sqlGenerator.getSelectComments(postId);
				ResultSet commentRS = executeQuerySQL(statement, connection);

				try
				{
					while (commentRS.next())
					{
						String commentId = commentRS.getString(ISQLGenerator.COMMENT_ID);
						String commentCreatorId = commentRS.getString(ISQLGenerator.CREATOR_ID);
						Date commentCreatedDate = commentRS.getTimestamp(ISQLGenerator.CREATED_DATE);
						Date commentModifiedDate = commentRS.getTimestamp(ISQLGenerator.MODIFIED_DATE);
						System.out.println("DATE !!!!!!! " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(commentModifiedDate));
						String content = commentRS.getString(ISQLGenerator.CONTENT);

						Comment comment = new Comment();
						comment.setId(commentId);
						comment.setPostId(post.getId());
						comment.setCreatorId(commentCreatorId);
						comment.setCreatedDate(commentCreatedDate);
						comment.setText(content);
						comment.setModifiedDate(commentModifiedDate);

						post.addComment(comment);
					}

					commentRS.close();
				}
				catch (SQLException sqle)
				{
					throw new PersistenceException(sqle);
				}

				statement = sqlGenerator.getSelectPostElements(postId);
				ResultSet structureRS = executeQuerySQL(statement, connection);

				try
				{
					while (structureRS.next())
					{
						String pId = structureRS.getString(ISQLGenerator.POST_ID);
						String elementId = structureRS.getString(ISQLGenerator.ELEMENT_ID);
						String displayName = structureRS.getString(ISQLGenerator.DISPLAY_NAME);
						int indentation = structureRS.getInt(ISQLGenerator.INDENTATION);
						String type = structureRS.getString(ISQLGenerator.ELEMENT_TYPE);

						if (type.equals(Paragraph.PARAGRAPH))
						{
							Paragraph paragraph = getParagraph(elementId, connection);
							paragraph.setIndentation(indentation);
							post.addElement(paragraph);
						}

						if (type.equals(Image.IMAGE))
						{
							Image image = getImage(elementId, Image.NONE, connection);
							image.setIndentation(indentation);
							post.addElement(image);
						}

						if (type.equals(File.FILE))
						{
							File file = getFile(elementId, connection);
							file.setDisplayName(displayName);
							file.setIndentation(indentation);
							post.addElement(file);
						}

						if (type.equals(LinkRule.LINK))
						{
							LinkRule link = getLink(elementId, connection);
							link.setDisplayName(displayName);
							link.setIndentation(indentation);
							post.addElement(link);
						}
					}

					structureRS.close();
				}
				catch (SQLException sqle)
				{
					throw new PersistenceException(sqle);
				}

				post.setDirty(false);

				result.add(post);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new PersistenceException(e.getMessage());
		}

		return result;
	}

	Image getImage(String imageId, int mode) throws PersistenceException
	{
		if (logger.isDebugEnabled())
			logger.debug("getImage(" + imageId + "," + mode + ")");

		Connection connection = getConnection();
		
		try
		{
			Image image = getImage(imageId, mode, connection);
			return image;
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	Image getImage(String imageId, int mode, Connection connection) throws PersistenceException
	{
		Image image = new Image();
		image.setId(imageId);
		try
		{
			ResultSet rs = executeQuerySQL(sqlGenerator.getSelectImage(imageId), connection);
			if (rs == null)
				return null;
			if (!rs.next())
				return null;

			String fullResourceId = rs.getString(ISQLGenerator.FULL_RESOURCE_ID);
			String webResourceId = rs.getString(ISQLGenerator.WEB_RESOURCE_ID);
			String fileName = rs.getString(ISQLGenerator.FILE_NAME);

			rs.close();

			image.setFullResourceId(fullResourceId);
			image.setWebResourceId(webResourceId);
			image.setFileName(fileName);

			// we only need recover the content
			if (mode == Image.ORIGINAL || mode == Image.ALL)
			{
				sakaiProxy.getImage(image, Image.ORIGINAL);
			}

			if (mode == Image.WEB || mode == Image.ALL)
			{
				sakaiProxy.getImage(image, Image.WEB);
			}

			return image;
		}
		catch (SQLException e)
		{
			logger.error("Caught exception whilst getting image", e);
			throw new PersistenceException(e.getMessage());
		}
	}

	private Paragraph getParagraph(String paragraphId, Connection connection) throws PersistenceException
	{
		ResultSet rs = executeQuerySQL(sqlGenerator.getSelectParagraph(paragraphId), connection);

		try
		{
			if (!rs.next())
			{
				rs.close();
				return null;
			}

			String content = rs.getString(ISQLGenerator.CONTENT);

			rs.close();

			Paragraph paragraph = new Paragraph(content);
			paragraph.setId(paragraphId);

			return paragraph;
		}
		catch (SQLException sqle)
		{
			return null;
		}
	}

	File getFile(String fileId) throws PersistenceException
	{
		if (logger.isDebugEnabled())
			logger.debug("getFile(" + fileId + ")");

		Connection connection = getConnection();

		try
		{
			File file = getFile(fileId, connection);
			return file;
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	File getFile(String fileId, Connection connection) throws PersistenceException
	{
		File file = new File();
		file.setId(fileId);
		try
		{
			ResultSet rs = executeQuerySQL(sqlGenerator.getSelectFile(fileId), connection);

			if (rs == null)
				return null;

			if (!rs.next())
			{
				rs.close();
				return null;
			}

			// we only need recover the content
			String fileName = rs.getString(ISQLGenerator.FILE_NAME);
			file.setFileName(fileName);

			String resourceId = rs.getString(ISQLGenerator.RESOURCE_ID);
			file.setResourceId(resourceId);

			String mimeType = rs.getString(ISQLGenerator.MIME_TYPE);
			file.setMimeType(mimeType);

			rs.close();

			return file;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new PersistenceException(e.getMessage());
		}
	}

	private LinkRule getLink(String linkId, Connection connection) throws PersistenceException
	{
		ResultSet rs = executeQuerySQL(sqlGenerator.getSelectLink(linkId), connection);

		try
		{
			if (!rs.next())
			{
				rs.close();
				return null;
			}

			String url = rs.getString(ISQLGenerator.URL);

			rs.close();

			LinkRule link = new LinkRule(linkId);
			link.setUrl(url);

			return link;
		}
		catch (SQLException sqle)
		{
			return null;
		}
	}

	private void releaseConnection(Connection connection)
	{
		if (logger.isDebugEnabled())
			logger.debug("releaseConnection()");

		try
		{
			sakaiProxy.returnConnection(connection);
		}
		catch (Exception e)
		{
			// we did our best...
		}
	}

	private Connection getConnection() throws PersistenceException
	{
		if (logger.isDebugEnabled())
			logger.debug("getConnection()");

		try
		{
			return sakaiProxy.borrowConnection();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new PersistenceException(e.getMessage());
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

	public void setSecurityManager(BlogSecurityManager securityManager)
	{
		this.securityManager = securityManager;
	}

	public BlogSecurityManager getSecurityManager()
	{
		return securityManager;
	}

	public List<BlogMember> getPublicBloggers()
	{
		List<BlogMember> members = new ArrayList<BlogMember>();

		String publicQuery = sqlGenerator.getSelectPublicBloggers();
		Connection connection = null;

		try
		{
			connection = getConnection();
			ResultSet rs = executeQuerySQL(publicQuery, connection);

			while (rs.next())
			{
				String userId = rs.getString(ISQLGenerator.CREATOR_ID);
				BlogMember member = sakaiProxy.getMember(userId);
				String countPostsQuery = sqlGenerator.getCountPublicPosts(userId);
				ResultSet countRS = executeQuerySQL(countPostsQuery, connection);

				countRS.next();

				int count = countRS.getInt("NUMBER_POSTS");

				countRS.close();
				member.setNumberOfPosts(count);

				members.add(member);
			}

			rs.close();
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst getting public bloggers.", e);
		}
		finally
		{
			releaseConnection(connection);
		}
		return members;
	}

	public void updateComment(Comment comment)
	{
		Connection connection = null;
		try
		{
			connection = getConnection();
			String sql = sqlGenerator.getUpdateStatementForComment(comment, connection);
			executeSQL(sql, connection);
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst adding comment.", e);
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public void removeResourceReference(String resourceId)
	{
		Connection connection = null;

		try
		{
			connection = getConnection();
			boolean oldAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			
			try
			{
				Collection statements = sqlGenerator.getRemoveResourceStatements(resourceId, connection);
				executeSQL(statements, connection);
				connection.commit();
			}
			catch(SQLException sqle)
			{
				connection.rollback();
			}
			finally
			{
				connection.setAutoCommit(oldAutoCommit);
			}
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst removing resource references.", e);
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public void updatePostElement(Post post, PostElement element, int elementIndex) throws Exception
	{
		// We need this in case the db stuff fails and we need to roll back
		List<String> successfulResourceIds = new ArrayList<String>();

		if (element instanceof File)
		{
			if (((File) element).isContentChanged())
			{
				removeFile((File) element);
				storeFile(post,(File) element,successfulResourceIds);
			}
		}
		else if (element instanceof Image)
		{
			if (((Image) element).isContentChanged())
			{
				removeImage((Image) element);
				storeImage(post,(Image) element,successfulResourceIds);
			}
		}

		Connection connection = null;
		try
		{
			connection = getConnection();
			boolean oldAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			
			try
			{
				Collection statements = sqlGenerator.getUpdateElementStatements(post, element, elementIndex, connection);
				executeSQL(statements, connection);
				connection.commit();
			}
			catch(SQLException sqle)
			{
				connection.rollback();
			}
			finally
			{
				connection.setAutoCommit(oldAutoCommit);
			}
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public void addPostElement(Post post, PostElement element) throws Exception
	{
		// We need this in case the db stuff fails and we need to roll back
		List<String> successfulResourceIds = new ArrayList<String>();

		if (element instanceof File)
			storeFile(post,(File) element,successfulResourceIds);
		if (element instanceof Image)
			storeImage(post,(Image) element,successfulResourceIds);

		Connection connection = null;
		try
		{
			connection = getConnection();
			boolean oldAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			
			try
			{
				Collection statements = sqlGenerator.getAddElementStatements(post, element, connection);
				executeSQL(statements, connection);
				connection.commit();
			}
			catch(SQLException sqle)
			{
				connection.rollback();
			}
			finally
			{
				connection.setAutoCommit(oldAutoCommit);
			}
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public void setIndentation(Post post, PostElement element, int i) throws Exception
	{
		Connection connection = null;
		try
		{
			connection = getConnection();
				String statements = sqlGenerator.getSetIndentationStatement(post, element, i, connection);
				executeSQL(statements, connection);
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public void swapElements(Post post, int first, int second) throws Exception
	{
		Connection connection = null;
		try
		{
			connection = getConnection();
			boolean oldAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			
			try
			{
				Collection statements = sqlGenerator.getSwapElementsStatements(post, first, second, connection);
				executeSQL(statements, connection);
				connection.commit();
			}
			catch(SQLException sqle)
			{
				connection.rollback();
			}
			finally
			{
				connection.setAutoCommit(oldAutoCommit);
			}
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public void deleteElement(Post post, int position) throws Exception
	{
		Connection connection = null;
		try
		{
			connection = getConnection();
			
			boolean oldAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			
			try
			{
				Collection statements = sqlGenerator.getDeleteStatementsForElement(post, position, connection);
				executeSQL(statements, connection);
				connection.commit();
			}
			catch(SQLException sqle)
			{
				connection.rollback();
			}
			finally
			{
				connection.setAutoCommit(oldAutoCommit);
			}
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public void saveTitle(Post post) throws Exception
	{
		Connection connection = null;
		try
		{
			connection = getConnection();
			String statement = sqlGenerator.getSaveTitleStatement(post);
			executeSQL(statement, connection);
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public void saveShortText(Post post) throws Exception
	{
		Connection connection = null;
		try
		{
			connection = getConnection();
			String statement = sqlGenerator.getSaveShortTextStatement(post);
			executeSQL(statement, connection);
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public void saveReadOnly(Post post) throws Exception
	{
		Connection connection = null;
		try
		{
			connection = getConnection();
			String statement = sqlGenerator.getSaveReadOnlyStatement(post);
			executeSQL(statement, connection);
		}
		finally
		{
			releaseConnection(connection);
		}
		
	}

	public void saveAllowComments(Post post) throws Exception
	{
		Connection connection = null;
		try
		{
			connection = getConnection();
			String statement = sqlGenerator.getSaveAllowCommentsStatement(post);
			executeSQL(statement, connection);
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public void saveVisibility(Post post) throws Exception
	{
		Connection connection = null;
		try
		{
			connection = getConnection();
			String statement = sqlGenerator.getSaveVisibilityStatement(post);
			executeSQL(statement, connection);
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public BlogOptions getOptions()
	{
		String placementId = sakaiProxy.getCurrentSiteId();
		
		BlogOptions settings = new BlogOptions();
		settings.setSiteId(placementId);
		
		Connection connection = null;
		try
		{
			connection = getConnection();
			String statement = sqlGenerator.getSelectOptionsStatement(sakaiProxy.getCurrentSiteId());
			ResultSet rs = executeQuerySQL(statement, connection);
			
			if(rs.next())
			{
				String mode = rs.getString(ISQLGenerator.MODE);
				settings.setMode(mode);
			}
			
			rs.close();
		}
		catch(Exception e)
		{
			logger.error("Caught exception whilst retrieving options",e);
		}
		finally
		{
			releaseConnection(connection);
		}
		
		return settings;
	}

	public void saveOptions(BlogOptions options) throws Exception
	{
		Connection connection = null;
		try
		{
			connection = getConnection();
			String statement = sqlGenerator.getSaveOptionsStatement(options,connection);
			executeSQL(statement, connection);
		}
		finally
		{
			releaseConnection(connection);
		}
	}
}
