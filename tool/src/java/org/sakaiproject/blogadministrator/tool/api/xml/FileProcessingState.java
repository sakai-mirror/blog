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

package org.sakaiproject.blogadministrator.tool.api.xml;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.sakaiproject.blogadministrator.tool.BlogDataMigratorApplication;
import org.sakaiproject.blogadministrator.tool.Migrator;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.datamodel.File;
import org.sakaiproject.blog.api.util.MimeTypeGuesser;
import org.sakaiproject.blog.api.SakaiProxy;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class FileProcessingState extends XMLPostContentHandleState
{
	private Post _post;

	private String _currentText;

	private File _fileUnderConstruction;

	private XMLPostContentHandleState _previousState;

	public FileProcessingState(String mode, Post post, XMLPostContentHandleState previousState)
	{
		super(mode);
		
		_currentText = new String();
		_previousState = previousState;
		_post = post;
		_fileUnderConstruction = new File();
	}

	public XMLPostContentHandleState startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
	{
		return this;
	}

	public XMLPostContentHandleState endElement(String uri, String localName, String qName) throws SAXException
	{
		if (qName.equals("file"))
		{
			_post.addElement(_fileUnderConstruction);
			return _previousState;
		}
		if (qName.equals("fileId"))
		{
			_fileUnderConstruction.setId(_currentText);
			loadFile();
			_currentText = new String();
			return this;
		}
		if (qName.equals("fileDescription"))
		{
			_fileUnderConstruction.setFileName(_currentText);
			_fileUnderConstruction.setDisplayName(_currentText);
			_fileUnderConstruction.setMimeType(MimeTypeGuesser.guessMimeType(_currentText));
			
			_currentText = new String();
			return this;

		}
		return this;
	}

	public XMLPostContentHandleState characters(char ch[], int start, int length) throws SAXException
	{
		if (_currentText == null)
		{
			_currentText = new String(ch, start, length);
		}
		else
		{
			_currentText += new String(ch, start, length);
		}
		return this;
	}
	
	private void loadFile()
	{
		SakaiProxy sakaiProxy = BlogDataMigratorApplication.get().getSakaiProxy();
		
		Connection connection = null;
		Statement statement = null;
		ResultSet fileRS = null;
		
		try
		{
			connection = sakaiProxy.borrowConnection();
			
			statement = connection.createStatement();
			
			if(mode.equals(Migrator.BLOG))
				fileRS = statement.executeQuery("SELECT * FROM BLOGGER_FILE WHERE FILE_ID = '" + _currentText + "'");
			else if(mode.equals(Migrator.LEARNINGLOG))
				fileRS = statement.executeQuery("SELECT * FROM LEARNINGLOG_FILE WHERE FILE_ID = '" + _currentText + "'");
			
			if(!fileRS.next())
				return;
			
			Blob fullContent = fileRS.getBlob("FILE_CONTENT");
			
			_fileUnderConstruction.setContent(fullContent.getBytes(1, (int) fullContent.length()));
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
		}
		finally
		{
			try
			{
				if(statement != null) statement.close();
				if(fileRS != null) fileRS.close();
			}
			catch(SQLException e) {}
			
			sakaiProxy.returnConnection(connection);
		}
	}
}
