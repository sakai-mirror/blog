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

package uk.ac.lancs.e_science.sakaiproject.impl.blogger.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import java.sql.*;

import org.apache.log4j.Logger;
import org.sakaiproject.db.api.SqlService;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.Blogger;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Creator;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.File;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Image;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.xml.XMLToPost;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.searcher.QueryBean;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.persistence.sql.util.*;

public class SakaiPersistenceManager
{
	private Logger logger = Logger.getLogger(SakaiPersistenceManager.class);
	
	private SqlService sqlService;

	ISQLGenerator sqlGenerator;

	public SakaiPersistenceManager() throws PersistenceException
	{
		if(logger.isDebugEnabled()) logger.debug("SakaiPersistenceManager()");
		
		sqlService = org.sakaiproject.db.cover.SqlService.getInstance();
		String vendor = sqlService.getVendor();
		// TODO load the proper class using reflection. We can use a named based system to locate the correct SQLGenerator
		if (vendor.equals("mysql"))
			sqlGenerator = new MySQLGenerator();
		else if (vendor.equals("oracle"))
			sqlGenerator = new SQLGenerator();
		else if (vendor.equals("hsqldb"))
			sqlGenerator = new HiperSonicGenerator();
      else if (vendor.equals("db2"))
         sqlGenerator = new DB2Generator();
		else
			throw new PersistenceException("Unknown database vendor:" + vendor);

	}

	public void storePost(Post post, String siteId) throws PersistenceException
	{
		if(logger.isDebugEnabled()) logger.debug("storePost(Post instance supplied with ID: " + post.getOID() + "," + siteId + ")");
		
		Connection connection = getConnection();
		try
		{
			Collection sqlStatements;
			if (!existPost(post.getOID()))
			{
				try
				{
					if(logger.isDebugEnabled()) logger.debug("This is a new post. Getting insert statements for post ...");
					sqlStatements = sqlGenerator.getInsertStatementsForPost(post, siteId, connection);
				
					if(logger.isDebugEnabled()) logger.debug("Executing insert statements for post ...");
					executeSQL(sqlStatements, connection);
				}
				catch(Exception e)
				{
					logger.error("Caught exception whilst inserting new post.",e);
					
					throw new PersistenceException(e.getMessage());
				}
			}
			else
			{
				// delete and insert again. this is less efficient but simpler
				
				// TODO: All of this needs to be in a transaction. The post can
				// be deleted, the insert can fail and the delete never gets
				// rolled back, arghhhhh
				
				Post originalPost = getPost(post.getOID());
				
				boolean oldAutoCommitFlag = true;
				
				try
				{
					if(logger.isDebugEnabled()) logger.debug("Getting delete statements for post ...");
					sqlStatements = sqlGenerator.getDeleteStatementsForPostExcludingImagesAndFiles(post.getOID());
				
					// Start transaction
					oldAutoCommitFlag = connection.getAutoCommit();
					connection.setAutoCommit(false);
				
					if(logger.isDebugEnabled()) logger.debug("Executing delete statements for post ...");
					executeSQL(sqlStatements, connection);
					
					if(logger.isDebugEnabled()) logger.debug("Getting insert statements for post ...");
					sqlStatements = sqlGenerator.getInsertStatementsForPostExcludingImagesAndFiles(post, siteId,connection);
					
					if(logger.isDebugEnabled()) logger.debug("Executing insert statements for post ...");
					executeSQL(sqlStatements, connection);
					
					reinsertImagesAndFiles(post, connection);
				}
				catch (Exception e)
				{
					// This can happen when the post has odd characters that we didn't deal with. But we tried our best!!!
					
					if(logger.isDebugEnabled()) e.printStackTrace();
					
					// Roll back !
					if(logger.isDebugEnabled()) logger.debug("Rolling back ...");
					try
					{
						connection.rollback();
					}
					catch (SQLException e1)
					{
						logger.error("Caught exception whilst rolling back post transaction. Message: " + e1.getMessage());
						
						if(logger.isDebugEnabled()) e1.printStackTrace();
					}
					
					logger.error("Caught an exception whilst inserting post. Message: " + e.getMessage());
					
				}
				finally
				{
					try
					{
						connection.setAutoCommit(oldAutoCommitFlag);
					}
					catch (SQLException e)
					{
						logger.error("Caught exception whilst resetting autocommit flag on db connection. Message: " + e.getMessage());
						
						if(logger.isDebugEnabled()) e.printStackTrace();
					}
				}
			}
		}
		finally
		{
			releaseConnection(connection);
		}
	}
	
	private void reinsertImagesAndFiles(Post post,Connection connection) throws PersistenceException
	{
		try
		{
			// Now... Images ... We need be more efficient.
			List<String> imagesIdInDb = getIdImages(post);
			for(String imageId : imagesIdInDb)
			{
				if (!post.hasImage(imageId))
					executeSQL(sqlGenerator.getDeleteStatementForImage(imageId), connection);
			}
			
			if (post.getImages() != null)
			{
				ArrayList imagesToInsert = new ArrayList();
				for (Image image : post.getImages())
				{
					if (!imagesIdInDb.contains(image.getIdImage()))
						imagesToInsert.add(image);
				}
				
				executeSQL(sqlGenerator.getInsertStatementsForImages((Image[]) imagesToInsert.toArray(new Image[0]), post.getOID(), connection), connection);
			}
			
			// Now... Files ... We need be more efficient.
			List<String> filesIdInDb = getIdFiles(post);
			for(String fileId : filesIdInDb)
			{
				if (!post.hasFile(fileId))
					executeSQL(sqlGenerator.getDeleteStatementForFile(fileId), connection);
			}
			
			if (post.getFiles() != null)
			{
				ArrayList filesToInsert = new ArrayList();
				for (int i = 0; i < post.getFiles().length; i++)
				{
					if (!filesIdInDb.contains(post.getFiles()[i].getIdFile()))
						filesToInsert.add(post.getFiles()[i]);
				}
				
				executeSQL(sqlGenerator.getInsertStatementsForFiles((File[]) filesToInsert.toArray(new File[0]), post.getOID(), connection), connection);
			}
		}
		catch(Exception e)
		{
			throw new PersistenceException(e.getMessage());
		}
	}

	public void deletePost(String postId) throws PersistenceException
	{
		if(logger.isDebugEnabled()) logger.debug("deletePost(" + postId + ")");
		
		Connection connection = getConnection();
		try
		{
			Collection sqlStatements;
			sqlStatements = sqlGenerator.getDeleteStatementsForPost(postId);

			executeSQL(sqlStatements, connection);
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public List getPosts(QueryBean query, String siteId) throws PersistenceException
	{
		if(logger.isDebugEnabled()) logger.debug("getPosts(" + query.getQueryString() + "," + siteId + ")");
		
		Connection connection = getConnection();
		try
		{
			String statement = sqlGenerator.getSelectStatementForQuery(query, siteId);
			ResultSet rs = executeQuerySQL(statement, connection);
			List result = transformResultSetInPostCollection(rs, false, false);
			return result;
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public Post getPost(String postId) throws PersistenceException
	{
		if(logger.isDebugEnabled()) logger.debug("getPost(" + postId + ")");
		
		Connection connection = getConnection();
		try
		{
			String statement = sqlGenerator.getSelectPost(postId);
			ResultSet rs = executeQuerySQL(statement, connection);
			List result = transformResultSetInPostCollection(rs, true, true); // TODO: Do we need load the files?
			if (result.size() == 0) throw new PersistenceException("getPost: Unable to find post with id:" + postId);
			if (result.size() > 1) throw new PersistenceException("getPost: there are more than one post with id:" + postId);
			return (Post) result.get(0);
		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public List getAllPost(String siteId) throws PersistenceException
	{
		if(logger.isDebugEnabled()) logger.debug("getAllPost(" + siteId + ")");
		
		Connection connection = getConnection();
		try
		{
			ResultSet rs = executeQuerySQL(sqlGenerator.getSelectAllPost(siteId), connection);
			List result = transformResultSetInPostCollection(rs, false, false);
			return result;
		}
		finally
		{
			releaseConnection(connection);
		}

	}

	public boolean existPost(String OID) throws PersistenceException
	{
		if(logger.isDebugEnabled()) logger.debug("existPost(" + OID + ")");
		
		Connection connection = getConnection();
		try
		{
			try
			{
				ResultSet rs = executeQuerySQL(sqlGenerator.getSelectPost(OID), connection);
				return (rs.next());
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				throw new PersistenceException(e.getMessage());
			}

		}
		finally
		{
			releaseConnection(connection);
		}

	}

	/**
	 * 
	 * @param post
	 * @return Collection with the image's identifier currently in the database that belows to the post
	 * @throws PersistenceException
	 */
	public List<String> getIdImages(Post post) throws PersistenceException
	{
		if(logger.isDebugEnabled()) logger.debug("getIdImages(Post instance supplied with ID: " + post.getOID() + ")");
		
		List<String> result = new ArrayList<String>();
		Connection connection = getConnection();
		try
		{
			try
			{
				ResultSet rs = executeQuerySQL(sqlGenerator.getSelectIdImagesFromPost(post), connection);
				while (rs.next())
				{
					result.add(rs.getString(1).trim());
				}
				return result;
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				throw new PersistenceException(e.getMessage());
			}

		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public Image getImage(String imageId, int size) throws PersistenceException
	{
		if(logger.isDebugEnabled()) logger.debug("getImage(" + imageId + "," + size + ")");
		
		Connection connection = getConnection();
		Image image = new Image();
		image.setIdImage(imageId);
		try
		{
			try
			{
				ResultSet rs = executeQuerySQL(sqlGenerator.getSelectImage(imageId), connection);
				if (rs == null) return null;
				if (!rs.next()) return null;
				// we only need recover the content
				if (size == Blogger.ORIGINAL || size == Blogger.ALL)
				{
					Blob blob = rs.getBlob(ISQLGenerator.IMAGE_CONTENT);
					int length = (int) blob.length();
					byte[] b = blob.getBytes(1, length);
					image.setContent(b);
				}
				if (size == Blogger.THUMBNAIL || size == Blogger.ALL)
				{
					Blob blob = rs.getBlob(ISQLGenerator.THUMBNAIL_IMAGE);
					int length = (int) blob.length();
					byte[] b = blob.getBytes(1, length);
					image.setImageContentWithThumbnailSize(b);
				}
				if (size == Blogger.WEB || size == Blogger.ALL)
				{
					Blob blob = rs.getBlob(ISQLGenerator.WEBSIZE_IMAGE);
					int length = (int) blob.length();
					byte[] b = blob.getBytes(1, length);
					image.setImageContentWithWebSize(b);
				}
				return image;
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				throw new PersistenceException(e.getMessage());
			}

		}
		finally
		{
			releaseConnection(connection);
		}
	}

	/**
	 * 
	 * @param post
	 * @return Collection with the file's identifier currently in the database that belows to the post
	 * @throws PersistenceException
	 */
	public List<String> getIdFiles(Post post) throws PersistenceException
	{
		if(logger.isDebugEnabled()) logger.debug("getIdFiles(Post instance supplied with ID: " + post.getOID() + ")");
		
		List<String> result = new ArrayList<String>();
		Connection connection = getConnection();
		try
		{
			try
			{
				ResultSet rs = executeQuerySQL(sqlGenerator.getSelectIdFilesFromPost(post), connection);
				while (rs.next())
				{
					result.add(rs.getString(1).trim());
				}
				return result;
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				throw new PersistenceException(e.getMessage());
			}

		}
		finally
		{
			releaseConnection(connection);
		}
	}

	public File getFile(String fileId) throws PersistenceException
	{
		if(logger.isDebugEnabled()) logger.debug("getFile(" + fileId + ")");
		
		Connection connection = getConnection();
		File file = new File();
		file.setIdFile(fileId);
		try
		{
			try
			{
				ResultSet rs = executeQuerySQL(sqlGenerator.getSelectFile(fileId), connection);
				if (rs == null) return null;
				if (!rs.next()) return null;
				// we only need recover the content
				Blob blob = rs.getBlob(3);
				file.setContent(blob.getBytes(1, (int) blob.length()));
				return file;
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				throw new PersistenceException(e.getMessage());
			}

		}
		finally
		{
			releaseConnection(connection);
		}
	}

	private ResultSet executeQuerySQL(String sql, Connection connection) throws PersistenceException
	{
		if(logger.isDebugEnabled()) logger.debug("executeQuerySQL(" + sql + "," + connection + ")");
		
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
		if(logger.isDebugEnabled()) logger.debug("executeSQL(" + sql + "," + connection + ")");
		
		Collection sqlList = new ArrayList();
		sqlList.add(sql);
		executeSQL(sqlList, connection);
	}

	private void executeSQL(Collection sql, Connection connection) throws PersistenceException
	{
		if(logger.isDebugEnabled()) logger.debug("executeSQL(" + sql + "," + connection + ")");
		
		try
		{
			Iterator it = sql.iterator();
			boolean autocommit = connection.getAutoCommit();
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
					{
						connection.setAutoCommit(true);
						statement.executeUpdate(sqlSentence);
						connection.setAutoCommit(autocommit);
					}
				}
				else if (sentence instanceof PreparedStatement)
				{
					try
					{
						PreparedStatement statement = (PreparedStatement) sentence;
						// we use prepared statements to insert or update data with BLOB
						connection.setAutoCommit(true);
						statement.executeUpdate();
						connection.setAutoCommit(autocommit);
					}
					catch (SQLException e)
					{
						System.out.println("Exception in Prepared statement");
						System.out.println("SQLException:" + e.getMessage());

					}
				}

			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new PersistenceException(e.getMessage());
		}
	}

	private List transformResultSetInPostCollection(ResultSet rs, boolean loadImages, boolean loadFiles) throws PersistenceException
	{
		ArrayList result = new ArrayList();
		if (rs == null) return result;
		XMLToPost xmlToPost = new XMLToPost();
		try
		{
			while (rs.next())
			{
				String xml = rs.getString(ISQLGenerator.XMLCOLUMN);
				Post post = xmlToPost.convertXMLInPost(xml.replaceAll(SQLGenerator.APOSTROFE, "'"));

				if (post != null)
				{ // post can be null if was imposible to parse the xml document. This can happend when the xml has a invalid caracter like (0x1a)
					/*
					 * if (loadImages) recoverImages(post); if (loadFiles) recoverFiles(post);
					 */

					String id = rs.getString(ISQLGenerator.POST_ID);
					post.setOID(id);

					// We need to override the creator id that was extracted from
					// the XML. The creator id in the xml may well be the sakai eid
					// which is undesirable. We really want the id from the
					// IDCREATOR field.
					String creatorId = rs.getString(ISQLGenerator.IDCREATOR);
					post.setCreator(new Creator(creatorId));

					String title = rs.getString(ISQLGenerator.TITLE);
					post.setTitle(title);

					long date = rs.getLong(ISQLGenerator.DATE);
					post.setDate(date);

					result.add(post);
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new PersistenceException(e.getMessage());
		}
		return result;
	}

	// this method is used when we need recover a post. We don't need recover all sizes of images.
	/*
	 * private void recoverImages(Post post) throws PersistenceException{ if (post.getElements()!=null){ for (PostElement element :post.getElements()){ if (element instanceof Image){ Image imageInDB =
	 * getImage(((Image)element).getIdImage()); ((Image)element).setContent(imageInDB.getContent()); ((Image)element).setThumbnail(imageInDB.getThumbnail());
	 * ((Image)element).setWebsize(imageInDB.getWebsize()); } } } }
	 * 
	 * private void recoverFiles(Post post) throws PersistenceException{ if (post.getElements()!=null){ for (PostElement element :post.getElements()){ if (element instanceof File){ File fileInDB =
	 * getFile(((File)element).getIdFile()); ((File)element).setContent(fileInDB.getContent()); } } } }
	 */
	private void releaseConnection(Connection connection)
	{
		if(logger.isDebugEnabled()) logger.debug("releaseConnection()");
		
		try
		{
			sqlService.returnConnection(connection);
		}
		catch (Exception e)
		{
			// we did our best...
		}
	}

	private Connection getConnection() throws PersistenceException
	{
		if(logger.isDebugEnabled()) logger.debug("getConnection()");
		
		try
		{
			return sqlService.borrowConnection();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new PersistenceException(e.getMessage());
		}
	}

	public void initRepository() throws PersistenceException
	{
		if(logger.isDebugEnabled()) logger.debug("initRepository()");
		
		Connection connection = getConnection();
		try
		{
			Collection statements = sqlGenerator.getCreateStatementsForPost();
			executeSQL(statements, connection);
		}
		catch (Exception e)
		{
			// e.printStackTrace();
		}
		finally
		{
			releaseConnection(connection);
		}

	}

}
