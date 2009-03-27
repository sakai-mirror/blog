package org.sakaiproject.blogadministrator.tool.api.xml;

import org.apache.log4j.Logger;
import org.sakaiproject.blog.api.datamodel.Post;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class CreatorProcessingState extends XMLPostContentHandleState
{
	private Logger logger = Logger.getLogger(CreatorProcessingState.class);
	
	private Post _post;
	private String _currentText;
	private XMLPostContentHandleState _previousState;

	public CreatorProcessingState(Post post, XMLPostContentHandleState previousState)
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
		if (qName.equals("creator"))
		{
			return _previousState;
		}
		if (qName.equals("idCreator"))
		{
			_post.setCreatorId(sakaiProxy.getIdForEid(_currentText));
			
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
}
