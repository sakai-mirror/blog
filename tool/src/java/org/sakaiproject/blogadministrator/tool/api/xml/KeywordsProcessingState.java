package org.sakaiproject.blogadministrator.tool.api.xml;

import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.sql.ISQLGenerator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class KeywordsProcessingState extends XMLPostContentHandleState
{
	private Post _post;

	private String _currentText;

	private XMLPostContentHandleState _previousState;

	public KeywordsProcessingState(Post post, XMLPostContentHandleState previousState)
	{
		super();

		_currentText = new String();
		_previousState = previousState;
		_post = post;
	}

	public XMLPostContentHandleState startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
	{
		return this;
	}

	public XMLPostContentHandleState endElement(String uri, String localName, String qName) throws SAXException
	{
		if (qName.equals("keyword"))
		{
			_post.addKeyword(_currentText);
			_currentText = new String();
			return this;
		}
		
		if (qName.equals("keywordsList"))
		{
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
		
		_currentText = _currentText.replaceAll(ISQLGenerator.APOSTROFE, "'");
		return this;
	}
}
