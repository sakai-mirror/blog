package org.sakaiproject.blogadministrator.tool.api.xml;

import org.sakaiproject.blog.api.datamodel.Comment;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class CommentCreatorProcessingState extends XMLPostContentHandleState
{
	private Comment _comment;

	private String _currentText;

	private XMLPostContentHandleState _previousState;

	public CommentCreatorProcessingState(Comment comment, XMLPostContentHandleState previousState)
	{
		super();
		
		_currentText = new String();
		_previousState = previousState;
		_comment = comment;
	}

	public XMLPostContentHandleState startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
	{
		return this;
	}

	public XMLPostContentHandleState endElement(String uri, String localName, String qName) throws SAXException
	{
       if (qName.equals("commentCreator")){
            //_comment.setCreator(_creatorUnderConstruction);
            return _previousState;
        }
		if (qName.equals("idCommentCreator"))
		{
			_comment.setCreatorId(sakaiProxy.getIdForEid(_currentText));
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
