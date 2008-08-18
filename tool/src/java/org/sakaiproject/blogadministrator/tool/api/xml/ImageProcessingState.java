package org.sakaiproject.blogadministrator.tool.api.xml;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.sakaiproject.blog.api.datamodel.Image;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blogadministrator.tool.Migrator;
import org.sakaiproject.blogadministrator.tool.BlogDataMigratorApplication;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ImageProcessingState extends XMLPostContentHandleState
{
	private Post _post;

	private String _currentText;

	private Image _imageUnderConstruction;

	private XMLPostContentHandleState _previousState;

	public ImageProcessingState(String mode, Post post, XMLPostContentHandleState previousState)
	{
		super(mode);
		
		_currentText = new String();
		_previousState = previousState;
		_post = post;
		_imageUnderConstruction = new Image();
	}

	public XMLPostContentHandleState startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
	{
		return this;
	}

	public XMLPostContentHandleState endElement(String uri, String localName, String qName) throws SAXException
	{
		if (qName.equals("imageId"))
		{
			_imageUnderConstruction.setId(_currentText);
			loadImage();
			_currentText = new String();
			return this;
		}
		if (qName.equals("imageDescription"))
		{
			_imageUnderConstruction.setFileName(_currentText);
			// _imageUnderConstruction.setDescription(_currentText);
			_currentText = new String();
			return this;

		}
		
		if (qName.equals("image"))
		{
			_post.addElement(_imageUnderConstruction);
			//postManager.addElement(_post,_imageUnderConstruction);
			return _previousState;
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

	private void loadImage()
	{
		SakaiProxy sakaiProxy = BlogDataMigratorApplication.get().getSakaiProxy();

		Connection connection = null;
		Statement statement = null;
		ResultSet imageRS = null;

		try
		{
			connection = sakaiProxy.borrowConnection();

			statement = connection.createStatement();

			if(mode.equals(Migrator.BLOG))
				imageRS = statement.executeQuery("SELECT * FROM BLOGGER_IMAGE WHERE IMAGE_ID = '" + _currentText + "'");
			else if(mode.equals(Migrator.LEARNINGLOG))
				imageRS = statement.executeQuery("SELECT * FROM LEARNINGLOG_IMAGE WHERE IMAGE_ID = '" + _currentText + "'");

			if (!imageRS.next())
				return;

			Blob fullContent = imageRS.getBlob("IMAGE_CONTENT");
			_imageUnderConstruction.setFullContent(fullContent.getBytes(1, (int) fullContent.length()));
		}
		catch (SQLException sqle)
		{
			sqle.printStackTrace();
		}
		finally
		{
			try
			{
				if (statement != null)
					statement.close();
				if (imageRS != null)
					imageRS.close();
			}
			catch (SQLException e)
			{
			}

			sakaiProxy.returnConnection(connection);
		}
	}
}
