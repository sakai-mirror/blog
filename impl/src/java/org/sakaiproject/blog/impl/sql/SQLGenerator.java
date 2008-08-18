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

package org.sakaiproject.blog.impl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.sakaiproject.blog.api.QueryBean;
import org.sakaiproject.blog.api.sql.ISQLGenerator;
import org.sakaiproject.blog.api.datamodel.BlogOptions;
import org.sakaiproject.blog.api.datamodel.Comment;
import org.sakaiproject.blog.api.datamodel.File;
import org.sakaiproject.blog.api.datamodel.Image;
import org.sakaiproject.blog.api.datamodel.LinkRule;
import org.sakaiproject.blog.api.datamodel.Modes;
import org.sakaiproject.blog.api.datamodel.Paragraph;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.datamodel.PostElement;
import org.sakaiproject.blog.api.datamodel.Preferences;
import org.sakaiproject.blog.api.datamodel.State;

public class SQLGenerator implements ISQLGenerator
{
	private Logger logger = Logger.getLogger(SQLGenerator.class);
	
	// by default, oracle values
	public String BLOB = "BLOB";

	public String BIGINT = "NUMBER";

	public String CLOB = "CLOB";
	
	public String TIMESTAMP = "DATETIME";
	
	public String VARCHAR  = "VARCHAR";
	
	public String TEXT  = "TEXT";

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getCreateStatementsForPost(java.lang.String)
	 */
	public List<String> getCreateStatementsForPost()
	{
		ArrayList result = new ArrayList();

		result.add(doTableForPost());
		result.add(doTableForPostElements());
		result.add(doTableForImages());
		result.add(doTableForParagraph());
		result.add(doTableForLinks());
		result.add(doTableForComments());
		result.add(doTableForFiles());
		result.add(doTableForOptions());
		result.add(doTableForPreferences());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getSelectStatementForQuery(java.lang.String,
	 *      uk.ac.lancs.e_science.sakaiproject.service.blogger.searcher.QueryBean, java.lang.String)
	 */
	public List<String> getSelectStatementsForQuery(QueryBean query)
	{
		List<String> statements = new ArrayList<String>();
		
		String queryString = query.getQueryString();
		
		if(queryString != null && queryString.length() > 0)
		{
			String sql = "SELECT *"
						+ " FROM " + TABLE_POST
							+ " WHERE (" + TITLE + " LIKE '%" + queryString + "%'"
							+ " OR " + SHORT_TEXT + " like '%" + queryString + "%')";
			
			if(query.queryBySiteId())
				sql += " AND " + SITE_ID + " = '" + query.getSiteId() + "'";
			
			sql += " ORDER BY " + CREATED_DATE + " DESC";
			
			statements.add(sql);
			
			sql = "SELECT DISTINCT " + TABLE_POST + ".*"
						+ " FROM " + TABLE_POST + "," + TABLE_PARAGRAPH + "," + TABLE_POST_ELEMENT
							+ " WHERE " + TABLE_POST + "." + POST_ID + " = " + TABLE_POST_ELEMENT + "." + POST_ID;
			
			if(query.queryBySiteId())
				sql += " AND " + TABLE_POST + "." + SITE_ID + " = '" + query.getSiteId() + "'";
			
			sql += " AND " + TABLE_POST_ELEMENT + "." + ELEMENT_ID + " = " + TABLE_PARAGRAPH + "." + PARAGRAPH_ID
				+ " AND " + TABLE_PARAGRAPH + "." + CONTENT + " like '%" + queryString + "%'"
				+ " ORDER BY " + CREATED_DATE + " DESC";
			
			statements.add(sql);
			
			sql = "SELECT DISTINCT " + TABLE_POST + ".*"
						+ " FROM " + TABLE_POST + "," + TABLE_COMMENT
							+ " WHERE " + TABLE_POST + "." + POST_ID + " = " + TABLE_COMMENT + "." + POST_ID;
			
			if(query.queryBySiteId())
				sql += " AND " + TABLE_POST + "." + SITE_ID + " = '" + query.getSiteId() + "'";
			
			sql += " AND " + TABLE_COMMENT + "." + CONTENT + " like '%" + queryString + "%'"
				+ " ORDER BY " + CREATED_DATE + " DESC";
			
			statements.add(sql);
			
			return statements;
		}
		
		StringBuilder statement = new StringBuilder();
		statement.append("SELECT * FROM ").append(TABLE_POST);
		
		if(query.hasConditions())
			statement.append(" WHERE ");

		// we know that there are conditions. Build the statement
		if (query.queryBySiteId())
			statement.append(SITE_ID).append(" = '").append(query.getSiteId()).append("' AND ");

		if(query.queryByCreator())
			statement.append(CREATOR_ID).append(" = '").append(query.getCreator()).append("' AND");
		
		if(query.queryByVisibility())
		{
			statement.append("(");

			String[] visibilities = query.getVisibilities();
			for (int i = 0; i < visibilities.length; i++)
			{
				statement.append(VISIBILITY).append("='").append(visibilities[i]).append("'");

				if (i < (visibilities.length - 1))
				{
					statement.append(" OR ");
				}
			}

			statement.append(") AND ");
		}
		
		if (query.queryByInitDate())
			statement.append(CREATED_DATE).append(">='").append(query.getInitDate()).append("' AND ");
		
		if (query.queryByEndDate())
			statement.append(CREATED_DATE).append("<='").append(query.getEndDate()).append("' AND ");
		

		// in this point, we know that there is a AND at the end of the statement. Remove it.
		statement = new StringBuilder(statement.toString().substring(0, statement.length() - 4)); // 4 is the length of AND with the last space
		statement.append(" ORDER BY ").append(CREATED_DATE).append(" DESC ");
		
		statements.add(statement.toString());
		return statements;
	}

	protected String doTableForPost()
	{
		StringBuilder statement = new StringBuilder();
		statement.append("CREATE TABLE ").append(TABLE_POST);
		statement.append("(");
		statement.append(POST_ID + " CHAR(36) NOT NULL,");
		statement.append(SITE_ID + " " + VARCHAR + "(255), ");
		statement.append(TITLE + " " + VARCHAR + "(255) NOT NULL, ");
		statement.append(CREATED_DATE + " " + TIMESTAMP + " NOT NULL" + ", ");
		statement.append(MODIFIED_DATE + " " + TIMESTAMP + " NOT NULL" + ", ");
		statement.append(CREATOR_ID + " " + VARCHAR + "(255) NOT NULL, ");
		statement.append(SHORT_TEXT + " " + TEXT + ", ");
		statement.append(KEYWORDS + " " + VARCHAR + "(255), ");
		statement.append(READ_ONLY + " INT, ");
		statement.append(ALLOW_COMMENTS + " INT, ");
		statement.append(VISIBILITY + " " + VARCHAR + "(16) NOT NULL, ");
		statement.append("CONSTRAINT post_pk PRIMARY KEY (" + POST_ID + ")");
		statement.append(")");
		return statement.toString();
	}

	protected String doTableForPostElements()
	{
		StringBuilder statement = new StringBuilder();
		statement.append("CREATE TABLE ").append(TABLE_POST_ELEMENT);
		statement.append("(");
		statement.append(POST_ID + " CHAR(36) NOT NULL,");
		statement.append(ELEMENT_ID + " CHAR(36) NOT NULL,");
		statement.append(ELEMENT_TYPE + " " + VARCHAR + "(36) NOT NULL,");
		statement.append(DISPLAY_NAME + " " + VARCHAR + "(255),");
		statement.append(INDENTATION + " INT NOT NULL,");
		statement.append(POSITION + " INT NOT NULL,");
		statement.append("CONSTRAINT post_element_pk PRIMARY KEY (" + POST_ID + "," + POSITION + ")");
		statement.append(")");
		return statement.toString();
	}

	protected String doTableForParagraph()
	{
		StringBuilder statement = new StringBuilder();
		statement.append("CREATE TABLE ").append(TABLE_PARAGRAPH);
		statement.append("(");
		statement.append(PARAGRAPH_ID + " CHAR(36) NOT NULL,");
		statement.append(CONTENT + " " + CLOB + " NOT NULL, ");
		statement.append("CONSTRAINT paragraph_pk PRIMARY KEY (" + PARAGRAPH_ID + ")");
		statement.append(")");
		return statement.toString();
	}

	protected String doTableForLinks()
	{
		StringBuilder statement = new StringBuilder();
		statement.append("CREATE TABLE ").append(TABLE_LINK);
		statement.append("(");
		statement.append(LINK_ID + " CHAR(36) NOT NULL,");
		statement.append(URL + " " + CLOB + " NOT NULL, ");
		statement.append("CONSTRAINT link_pk PRIMARY KEY (" + LINK_ID + ")");
		statement.append(")");
		return statement.toString();
	}

	protected String doTableForImages()
	{
		StringBuilder statement = new StringBuilder();
		statement.append("CREATE TABLE ").append(TABLE_IMAGE);
		statement.append("(");
		statement.append(IMAGE_ID + " CHAR(36),");
		statement.append(FILE_NAME + " " + VARCHAR + "(255) NOT NULL, ");
		statement.append(FULL_RESOURCE_ID + " " + VARCHAR + "(255) NOT NULL, ");
		statement.append(WEB_RESOURCE_ID + " " + VARCHAR + "(255) NOT NULL, ");
		statement.append("CONSTRAINT image_pk PRIMARY KEY (" + IMAGE_ID + ")");
		statement.append(")");
		return statement.toString();
	}

	protected String doTableForFiles()
	{
		StringBuilder statement = new StringBuilder();
		statement.append("CREATE TABLE ").append(TABLE_FILE);
		statement.append("(");
		statement.append(FILE_ID + " CHAR(36) NOT NULL,");
		statement.append(FILE_NAME + " " + VARCHAR + "(255) NOT NULL, ");
		statement.append(RESOURCE_ID + " " + VARCHAR + "(255) NOT NULL, ");
		statement.append(MIME_TYPE + " " + VARCHAR + "(255) NOT NULL, ");
		statement.append("CONSTRAINT file_pk PRIMARY KEY (" + FILE_ID + ")");
		statement.append(")");
		return statement.toString();
	}

	protected String doTableForComments()
	{
		StringBuilder statement = new StringBuilder();
		statement.append("CREATE TABLE ").append(TABLE_COMMENT);
		statement.append("(");
		statement.append(COMMENT_ID + " CHAR(36) NOT NULL,");
		statement.append(POST_ID + " CHAR(36) NOT NULL,");
		statement.append(CREATOR_ID + " CHAR(36) NOT NULL,");
		statement.append(CREATED_DATE + " " + TIMESTAMP + " NOT NULL,");
		statement.append(MODIFIED_DATE + " " + TIMESTAMP + " NOT NULL,");
		statement.append(CONTENT + " " + CLOB + " NOT NULL, ");
		statement.append("CONSTRAINT comment_pk PRIMARY KEY (" + COMMENT_ID + ")");
		statement.append(")");
		return statement.toString();
	}
	
	protected String doTableForOptions()
	{
		StringBuilder statement = new StringBuilder();
		statement.append("CREATE TABLE ").append(TABLE_OPTIONS);
		statement.append("(");
		statement.append(SITE_ID + " " + VARCHAR + "(255), ");
		statement.append(MODE + " " + VARCHAR + "(24) NOT NULL,");
		statement.append(TIMEOUT + " SMALLINT NOT NULL,");
		statement.append("CONSTRAINT options_pk PRIMARY KEY (" + SITE_ID + ")");
		statement.append(")");
		return statement.toString();
	}
	
	protected String doTableForPreferences()
	{
		StringBuilder statement = new StringBuilder();
		statement.append("CREATE TABLE ").append(TABLE_PREFERENCES);
		statement.append("(");
		statement.append(USER_ID + " " + VARCHAR + "(36), ");
		statement.append(SITE_ID + " " + VARCHAR + "(255), ");
		statement.append(NEW_POST_ALERT + " INT DEFAULT 1,");
		statement.append(NEW_COMMENT_ALERT + " INT DEFAULT 1,");
		statement.append("CONSTRAINT preferences_pk PRIMARY KEY (" + USER_ID + "," + SITE_ID + ")");
		statement.append(")");
		return statement.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getSelectAllPost(java.lang.String)
	 */
	public String getSelectComments(String postId)
	{
		return "SELECT * FROM " + TABLE_COMMENT + " WHERE " + POST_ID + "='" + postId + "' ORDER BY " + CREATED_DATE + " ASC";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getSelectAllPost(java.lang.String)
	 */
	public String getSelectAllPost(String placementId)
	{
		return "SELECT * FROM " + TABLE_POST + " WHERE " + SITE_ID + "='" + placementId + "' ORDER BY " + CREATED_DATE + " DESC";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getSelectPost(java.lang.String)
	 */
	public String getSelectPost(String OID)
	{
		return "SELECT * FROM " + TABLE_POST + " WHERE " + POST_ID + "='" + OID + "'";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getSelectImage(java.lang.String)
	 */
	public String getSelectImage(String imageId)
	{
		return "SELECT * FROM " + TABLE_IMAGE + " WHERE " + IMAGE_ID + "='" + imageId + "'";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getSelectFile(java.lang.String)
	 */
	public String getSelectFile(String fileId)
	{
		return "SELECT * FROM " + TABLE_FILE + " WHERE " + FILE_ID + "='" + fileId + "'";
	}

	public String getSelectParagraph(String paragraphId)
	{
		return "SELECT * FROM " + TABLE_PARAGRAPH + " WHERE " + PARAGRAPH_ID + "='" + paragraphId + "'";
	}

	public String getSelectLink(String linkId)
	{
		return "SELECT * FROM " + TABLE_LINK + " WHERE " + LINK_ID + "='" + linkId + "'";
	}

	public String getSelectIdImagesFromPost(Post post)
	{
		return "SELECT " + IMAGE_ID + " FROM " + TABLE_IMAGE + " WHERE " + POST_ID + "='" + post.getId() + "'";
	}

	public String getSelectIdFilesFromPost(Post post)
	{
		return "SELECT " + FILE_ID + " FROM " + TABLE_FILE + " WHERE " + POST_ID + "='" + post.getId() + "'";
	}

	public PreparedStatement getInsertStatementForComment(Comment comment, Connection connection) throws Exception
	{
		String sql = "INSERT INTO " + TABLE_COMMENT + " VALUES(?,?,?,?,?,?)";
		
		PreparedStatement statement = connection.prepareStatement(sql);
		
		statement.setString(1,comment.getId());
		statement.setString(2,comment.getPostId());
		statement.setString(3,comment.getCreatorId());
		statement.setTimestamp(4,new Timestamp(comment.getCreatedDate().getTime()));
		statement.setTimestamp(5,new Timestamp(comment.getModifiedDate().getTime()));
		statement.setString(6,comment.getText());

		return statement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getDeleteStatementsForPost(java.lang.String, java.lang.String)
	 */
	public List<String> getDeleteStatementsForPost(String postId, Connection connection)
	{
		List<String> result = new ArrayList<String>();
		StringBuilder statement = new StringBuilder("");

		result.addAll(getDeleteStatementsForPostElements(postId, connection));

		statement = new StringBuilder("");
		statement.append("DELETE FROM ").append(TABLE_COMMENT).append(" WHERE ");
		statement.append(POST_ID).append("='").append(postId).append("'");
		result.add(statement.toString());

		statement = new StringBuilder("");
		statement.append("DELETE FROM ").append(TABLE_POST_ELEMENT).append(" WHERE ");
		statement.append(POST_ID).append("='").append(postId).append("'");
		result.add(statement.toString());

		statement = new StringBuilder("");
		statement.append("DELETE FROM ").append(TABLE_POST).append(" WHERE ");
		statement.append(POST_ID).append("='").append(postId).append("'");
		result.add(statement.toString());

		return result;
	}
	
	public String getRecycleStatementForPost(String postId, Connection connection)
	{
		return "UPDATE " + TABLE_POST + " SET " + VISIBILITY + " = '" + State.RECYCLED + "' WHERE " + POST_ID + " = '" + postId + "'";
	}

	private List<String> getDeleteStatementsForPostElements(String postId, Connection connection)
	{
		List<String> statements = new ArrayList<String>();

		try
		{
			Statement structureST = connection.createStatement();
			ResultSet structureRS = structureST.executeQuery(this.getSelectPostElements(postId));

			while (structureRS.next())
			{
				String elementId = structureRS.getString(ELEMENT_ID);
				String elementType = structureRS.getString(ELEMENT_TYPE);

				if (elementType.equals(Paragraph.PARAGRAPH))
					statements.add("DELETE FROM " + TABLE_PARAGRAPH + " WHERE " + PARAGRAPH_ID + " = '" + elementId + "'");
				else if (elementType.equals(Image.IMAGE))
					statements.add("DELETE FROM " + TABLE_IMAGE + " WHERE " + IMAGE_ID + " = '" + elementId + "'");
				else if (elementType.equals(LinkRule.LINK))
					statements.add("DELETE FROM " + TABLE_LINK + " WHERE " + LINK_ID + " = '" + elementId + "'");
				else if (elementType.equals(File.FILE))
					statements.add("DELETE FROM " + TABLE_FILE + " WHERE " + FILE_ID + " = '" + elementId + "'");
			}

			structureRS.close();
			structureST.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return statements;
	}

	public PreparedStatement getInsertStatementForPost(Post post,Connection connection) throws Exception
	{
		String sql = "INSERT INTO " + TABLE_POST + " ("
		+ POST_ID + ","
		+ SITE_ID + ","
		+ TITLE + ","
		+ CREATED_DATE + ","
		+ MODIFIED_DATE + ","
		+ CREATOR_ID + ","
		+ VISIBILITY + ","
		+ SHORT_TEXT + ","
		+ KEYWORDS + ","
		+ READ_ONLY + ","
		+ ALLOW_COMMENTS
		+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement statement = connection.prepareStatement(sql);

		statement.setString(1,post.getId());

		statement.setString(2,post.getSiteId());

		statement.setString(3,post.getTitle());

		statement.setTimestamp(4,new Timestamp(post.getCreatedDate().getTime()));

		statement.setTimestamp(5,new Timestamp(post.getModifiedDate().getTime()));

		statement.setString(6,post.getCreatorId());

		statement.setString(7,post.getVisibility());

		statement.setString(8,post.getShortText());

		statement.setString(9,post.getKeywords());

		statement.setInt(10, (post.isReadOnly()) ? 1 : 0);

		statement.setInt(11,(post.isCommentable()) ? 1 : 0);

		return statement;
	}

	public List<String> getInsertStatementsForPostElement(Post post, Connection connection) throws Exception
	{
		List<String> result = new ArrayList<String>();

		int pos = 0;
		for (Iterator<PostElement> els = post.getElements(); els.hasNext();)
		{
			PostElement element = els.next();

			StringBuilder sqlStatement = new StringBuilder();
			sqlStatement.append("INSERT INTO ").append(TABLE_POST_ELEMENT).append(" (");
			sqlStatement.append(POST_ID + ",");
			sqlStatement.append(ELEMENT_ID + ",");
			sqlStatement.append(ELEMENT_TYPE + ",");
			sqlStatement.append(DISPLAY_NAME + ",");
			sqlStatement.append(INDENTATION + ",");
			sqlStatement.append(POSITION);
			sqlStatement.append(") VALUES (");
			sqlStatement.append("'").append(post.getId()).append("',");
			sqlStatement.append("'").append(element.getId()).append("',");
			sqlStatement.append("'").append(element.getType()).append("',");
			sqlStatement.append("'").append(element.getDisplayName().replaceAll("'",APOSTROFE)).append("',");
			sqlStatement.append("'").append(element.getIndentation()).append("',");
			sqlStatement.append("'").append(pos).append("')");
			result.add(sqlStatement.toString());
			pos++;
		}

		return result;
	}

	public String getSelectImages(String postId)
	{
		String sql = "SELECT * FROM " + TABLE_IMAGE + " WHERE " + POST_ID + " = '" + postId + "'";
		return sql;
	}

	public String getSelectFiles(String postId)
	{
		String sql = "SELECT * FROM " + TABLE_FILE + " WHERE " + POST_ID + " = '" + postId + "'";
		return sql;
	}

	public String getSelectLinks(String postId)
	{
		String sql = "SELECT * FROM " + TABLE_LINK + " WHERE " + POST_ID + " = '" + postId + "'";
		return sql;
	}

	public String getSelectPostElements(String postId)
	{
		String sql = "SELECT * FROM " + TABLE_POST_ELEMENT + " WHERE " + POST_ID + " = '" + postId + "' ORDER BY POSITION";
		return sql;
	}

	public String getSelectParagraphs(String postId)
	{
		String sql = "SELECT * FROM " + TABLE_PARAGRAPH + " WHERE " + POST_ID + " = '" + postId + "'";
		return sql;
	}

	public String getSelectPublicBloggers()
	{
		String sql = "SELECT DISTINCT " + CREATOR_ID + " FROM " + TABLE_POST + " WHERE " + VISIBILITY + " = '" + State.PUBLIC + "'";
		return sql;
	}

	public String getCountPublicPosts(String creatorId)
	{
		String sql = "SELECT COUNT(*) AS NUMBER_POSTS" + " FROM " + TABLE_POST + " WHERE " + CREATOR_ID + " = '" + creatorId + "'" + " AND " + VISIBILITY + " = '" + State.PUBLIC + "'";
		return sql;
	}

	public String getDeleteStatementForComment(Comment comment, Connection connection)
	{
		return "DELETE FROM " + TABLE_COMMENT + " WHERE " + COMMENT_ID + " = '" + comment.getId() + "' AND " + POST_ID + " = '" + comment.getPostId() + "'";
	}

	public String getUpdateStatementForComment(Comment comment, Connection connection)
	{
		String sql = "UPDATE " + TABLE_COMMENT + " SET " + MODIFIED_DATE + " = '" + new Timestamp(comment.getModifiedDate().getTime()) + "'," + CONTENT + " = '" + comment.getText().replaceAll("'", APOSTROFE) + "'" + " WHERE " + COMMENT_ID + " = '" + comment.getId() + "'";

		return sql;
	}

	public Collection getRemoveResourceStatements(String resourceId, Connection connection) throws Exception
	{
		if(resourceId.startsWith("/content"))
			resourceId = resourceId.substring("/content".length());
		
		List<String> statements = new ArrayList<String>();

		Statement statement = connection.createStatement();

		String sql = "SELECT " + FILE_ID + " FROM " + TABLE_FILE + " WHERE " + RESOURCE_ID + " = '" + resourceId + "'";

		ResultSet rs = statement.executeQuery(sql);

		if (rs.next())
		{
			String fileId = rs.getString(FILE_ID);
			statements.add("DELETE FROM " + TABLE_POST_ELEMENT + " WHERE " + ELEMENT_ID + " = '" + fileId + "'");
			statements.add("DELETE FROM " + TABLE_FILE + " WHERE " + FILE_ID + " = '" + fileId + "'");
		}

		rs.close();

		sql = "SELECT " + IMAGE_ID + " FROM " + TABLE_IMAGE + " WHERE " + FULL_RESOURCE_ID + " = '" + resourceId + "'" + " OR " + WEB_RESOURCE_ID + " = '" + resourceId + "'";
		rs = statement.executeQuery(sql);

		if (rs.next())
		{
			String imageId = rs.getString(IMAGE_ID);
			statements.add("DELETE FROM " + TABLE_POST_ELEMENT + " WHERE " + ELEMENT_ID + " = '" + imageId + "'");
			statements.add("DELETE FROM " + TABLE_IMAGE + " WHERE " + IMAGE_ID + " = '" + imageId + "'");
		}

		rs.close();
		statement.close();

		return statements;
	}

	public Collection getUpdateElementStatements(Post post, PostElement element, int elementIndex, Connection connection) throws Exception
	{
		List statements = new ArrayList();
		
		if(element instanceof Paragraph)
		{
			String sql = "UPDATE " + TABLE_PARAGRAPH
					+ " SET " + CONTENT + " = ?"
					+ " WHERE " + PARAGRAPH_ID + " = '" + element.getId() + "'";
			
			PreparedStatement st = connection.prepareStatement(sql);
			st.setString(1, ((Paragraph) element).getText());
			
			statements.add(st);
		}
		else if(element instanceof LinkRule)
		{
			String sql = "UPDATE " + TABLE_LINK
					+ " SET " + URL + " = ?"
					+ " WHERE " + LINK_ID + " = '" + element.getId() + "'";
			
			PreparedStatement st = connection.prepareStatement(sql);
			st.setString(1, ((LinkRule) element).getUrl());
			
			statements.add(st);
		}
		else if(element instanceof File)
		{
			String sql = "UPDATE " + TABLE_FILE
					+ " SET " + FILE_NAME + " = ?,"
					+ RESOURCE_ID + " = ?,"
					+ MIME_TYPE + " = ?"
					+ " WHERE " + FILE_ID + " = '" + element.getId() + "'";
			
			PreparedStatement st = connection.prepareStatement(sql);
			st.setString(1,((File)element).getFileName());
			st.setString(2,((File)element).getResourceId());
			st.setString(3,((File)element).getMimeType());
			
			statements.add(st);
		}
		else if(element instanceof Image)
		{
			String sql = "UPDATE " + TABLE_IMAGE
					+ " SET " + FILE_NAME + " = ?,"
					+ FULL_RESOURCE_ID + " = ?,"
					+ WEB_RESOURCE_ID + " = ?"
					+ " WHERE " + IMAGE_ID + " = '" + element.getId() + "'";
			
			PreparedStatement st = connection.prepareStatement(sql);
			st.setString(1,((Image)element).getFileName());
			st.setString(2,((Image)element).getFullResourceId());
			st.setString(3,((Image)element).getWebResourceId());
			
			statements.add(st);
		}
		
		statements.add("DELETE FROM " + TABLE_POST_ELEMENT + " WHERE " + POST_ID + " = '" + post.getId() + "'");
		statements.addAll(getInsertStatementsForPostElement(post, connection));
		
		return statements;
	}

	public Collection getAddElementStatements(Post module, PostElement element, Connection connection) throws Exception
	{
		List statements = new ArrayList();
		
		String type = "";
		String displayName = element.getDisplayName();
		
		if(element instanceof Paragraph)
		{
			String sql = "INSERT INTO " + TABLE_PARAGRAPH + " VALUES(?,?)";
			
			PreparedStatement st = connection.prepareStatement(sql);
			st.setString(1, element.getId());
			st.setString(2, ((Paragraph) element).getText());
			
			statements.add(st);
			
			type = Paragraph.PARAGRAPH;
		}
		else if(element instanceof LinkRule)
		{
			String sql = "INSERT INTO " + TABLE_LINK + " VALUES(?,?)";
			
			PreparedStatement st = connection.prepareStatement(sql);
			st.setString(1,element.getId());
			st.setString(2,((LinkRule) element).getUrl());
			
			statements.add(st);
			
			type = LinkRule.LINK;
		}
		else if(element instanceof File)
		{
			String sql = "INSERT INTO " + TABLE_FILE + " VALUES(?,?,?,?)";
			
			PreparedStatement st = connection.prepareStatement(sql);
			st.setString(1,element.getId());
			st.setString(2,((File)element).getFileName());
			st.setString(3,((File)element).getResourceId());
			st.setString(4,((File) element).getMimeType());
			
			statements.add(st);
			
			type = File.FILE;
		}
		else if(element instanceof Image)
		{
			String sql = "INSERT INTO " + TABLE_IMAGE + " VALUES(?,?,?,?)";
			
			PreparedStatement st = connection.prepareStatement(sql);
			st.setString(1,element.getId());
			st.setString(2,((Image)element).getFileName());
			st.setString(3,((Image)element).getFullResourceId());
			st.setString(4,((Image)element).getWebResourceId());
			
			statements.add(st);
			
			type = Image.IMAGE;
			
		}
		
		statements.add("DELETE FROM " + TABLE_POST_ELEMENT + " WHERE " + POST_ID + " = '" + module.getId() + "'");
		statements.addAll(getInsertStatementsForPostElement(module, connection));
		
		return statements;
	}

	public String getSetIndentationStatement(Post post, PostElement element, int i, Connection connection)
	{
		return "UPDATE " + TABLE_POST_ELEMENT + " SET " + INDENTATION + " = '" + i + "' WHERE " + POST_ID + " = '" + post.getId() + "' AND " + ELEMENT_ID + " = '" + element.getId() + "'";
	}

	public Collection getSwapElementsStatements(Post post, int first, int second, Connection connection) throws Exception
	{
		int temp = -1;
		
		List<String> statements = new ArrayList<String>();
		
		statements.add("UPDATE " + TABLE_POST_ELEMENT
						+ " SET " + POSITION + " = '" + temp + "' WHERE "
						+ POST_ID + " = '" + post.getId() + "'"
						+ " AND " + POSITION + " = " + first);
		
		statements.add("UPDATE " + TABLE_POST_ELEMENT
						+ " SET " + POSITION + " = '" + first + "' WHERE "
						+ POST_ID + " = '" + post.getId() + "'"
						+ " AND " + POSITION + " = " + second);
		
		statements.add("UPDATE " + TABLE_POST_ELEMENT
						+ " SET " + POSITION + " = '" + second + "' WHERE "
						+ POST_ID + " = '" + post.getId() + "'"
						+ " AND " + POSITION + " = " + temp);
		
		return statements;
	}

	public Collection getDeleteStatementsForElement(Post post, int position, Connection connection) throws Exception
	{
		List<String> statements = new ArrayList<String>();
		
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM " + TABLE_POST_ELEMENT
										+ " WHERE "
										+ POST_ID + " = '" + post.getId()
										+ "' AND " + POSITION + " = " + position);
		
		if(rs.next())
		{
			String elementId = rs.getString(ELEMENT_ID);
			String type = rs.getString(ELEMENT_TYPE);
			
			if(type.equals(Image.IMAGE))
			{
				statements.add("DELETE FROM " + TABLE_IMAGE
						+ " WHERE "
						+ IMAGE_ID + " = '" + elementId + "'");
			}
			else if(type.equals(Paragraph.PARAGRAPH))
			{
				statements.add("DELETE FROM " + TABLE_PARAGRAPH
						+ " WHERE "
						+ PARAGRAPH_ID + " = '" + elementId + "'");
			}
			else if(type.equals(File.FILE))
			{
				statements.add("DELETE FROM " + TABLE_FILE
						+ " WHERE "
						+ FILE_ID + " = '" + elementId + "'");
			}
			else if(type.equals(LinkRule.LINK))
			{
				statements.add("DELETE FROM " + TABLE_LINK
						+ " WHERE "
						+ LINK_ID + " = '" + elementId + "'");
			}
		}
		
		rs.close();
		st.close();
		
		statements.add("DELETE FROM " + TABLE_POST_ELEMENT + " WHERE " + POST_ID + " = '" + post.getId() + "'");
		statements.addAll(getInsertStatementsForPostElement(post, connection));
		
		return statements;
	}

	public String getSaveTitleStatement(Post post)
	{
		return "UPDATE " + TABLE_POST + " SET " + TITLE + " = '" + post.getTitle() + "' WHERE " + POST_ID + " = '" + post.getId() + "'";
	}

	public String getSaveShortTextStatement(Post post)
	{
		return "UPDATE " + TABLE_POST + " SET " + SHORT_TEXT + " = '" + post.getShortText() + "' WHERE " + POST_ID + " = '" + post.getId() + "'";
	}

	public String getSaveReadOnlyStatement(Post post)
	{
		int readOnly = (post.isReadOnly()) ? 1 : 0;
		return "UPDATE " + TABLE_POST + " SET " + READ_ONLY + " = " + readOnly + " WHERE " + POST_ID + " = '" + post.getId() + "'";
	}

	public String getSaveAllowCommentsStatement(Post post)
	{
		int allowComments = (post.isCommentable()) ? 1 : 0;
		return "UPDATE " + TABLE_POST + " SET " + ALLOW_COMMENTS + " = " + allowComments + " WHERE " + POST_ID + " = '" + post.getId() + "'";
	}

	public String getSaveVisibilityStatement(Post post)
	{
		return "UPDATE " + TABLE_POST + " SET " + VISIBILITY + " = '" + post.getVisibility() + "' WHERE " + POST_ID + " = '" + post.getId() + "'";
	}

	public String getSaveOptionsStatement(BlogOptions options,Connection connection) throws Exception
	{
		String mode = (options.isLearningLogMode()) ? Modes.LEARNING_LOG : Modes.BLOG;
		
		int timeout = (Integer.parseInt(options.getTimeoutHours()) * 60) + (Integer.parseInt(options.getTimeoutDays()) * 1440);
		
		String sql =  "UPDATE " + TABLE_OPTIONS + " SET " + MODE + " = '" + mode + "',TIMEOUT = " + timeout + " WHERE "
						+ SITE_ID + " = '" + options.getSiteId() + "'";
		
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM " + TABLE_OPTIONS + " WHERE " + SITE_ID + " = '" + options.getSiteId() + "'");
		
		if(!rs.next())
			sql = "INSERT INTO " + TABLE_OPTIONS + " VALUES('" + options.getSiteId() + "','" + mode + "'," + timeout + ")";
		
		rs.close();
		st.close();
		
		return sql;
	}

	public String getSelectOptionsStatement(String placementId)
	{
		return "SELECT * FROM " + TABLE_OPTIONS + " WHERE " + SITE_ID + " = '" + placementId + "'";
	}
	
	public String getSelectPreferencesStatement(String userId,String placementId)
	{
		return "SELECT * FROM " + TABLE_PREFERENCES + " WHERE " + USER_ID + " = '" + userId + "' AND " + SITE_ID + " = '" + placementId + "'";
	}

	public String getSavePreferencesStatement(Preferences preferences, Connection connection) throws Exception
	{
		String userId = preferences.getUserId();
		String siteId = preferences.getSiteId();
		int newPostAlert = (preferences.isNewPostAlert()) ? 1 : 0;
		int newCommentAlert = (preferences.isNewCommentAlert()) ? 1 : 0;
		String sql =  "UPDATE " + TABLE_PREFERENCES
					+ " SET " + NEW_POST_ALERT + " = " + newPostAlert + "," + NEW_COMMENT_ALERT + " = " + newCommentAlert
					+ " WHERE " + USER_ID + " = '" + userId + "' AND " + SITE_ID + " = '" + siteId + "'";
		
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM " + TABLE_PREFERENCES
					+ " WHERE " + USER_ID + " = '" + userId + "' AND " + SITE_ID + " = '" + siteId + "'");
		
		if(!rs.next())
			sql = "INSERT INTO " + TABLE_PREFERENCES + " VALUES('" + userId + "','" + siteId + "'," + newPostAlert + "," + newCommentAlert + ")";
		
		rs.close();
		st.close();
		
		return sql;
	}

	public String getSelectNewPostEmailUnSubscribersStatement(String siteId)
	{
		return "SELECT " + USER_ID + " FROM " + TABLE_PREFERENCES + " WHERE " + SITE_ID + " = '" + siteId + "' AND " + NEW_POST_ALERT + " = 0";
	}

	public String getSelectNewCommentUnSubscribersStatement(String siteId)
	{
		return "SELECT " + USER_ID + " FROM " + TABLE_PREFERENCES + " WHERE " + SITE_ID + " = '" + siteId + "' AND " + NEW_COMMENT_ALERT + " = 0";
	}
}
