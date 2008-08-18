package org.sakaiproject.blogadministrator.tool.api.xml;

import java.util.Date;

import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.datamodel.Comment;
import org.sakaiproject.blog.api.sql.ISQLGenerator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class CommentsProcessingState extends XMLPostContentHandleState
{
	private Post _post;

	private String _currentText;

	private Comment _commentUnderConstruction;

	private XMLPostContentHandleState _previousState;

	public CommentsProcessingState(Post post, XMLPostContentHandleState previousState)
	{
		super();
		
		_currentText = new String();
		_previousState = previousState;
		_post = post;
		_commentUnderConstruction = new Comment();
	}

	public XMLPostContentHandleState startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
	{
		if (qName.equals("commentCreator"))
		{
			return new CommentCreatorProcessingState(_commentUnderConstruction, this);
		}
		if (!isKnowTag(qName))
		{
			// Non expected tags, probabily they are html tags. We will add them to the text
			_currentText += "<" + qName + " ";
			int numberAttributes = atts.getLength();
			for (int i = 0; i < numberAttributes; i++)
			{
				String attName = atts.getLocalName(i);
				String attValue = atts.getValue(i);
				_currentText += attName + "=\"" + attValue + "\" ";
			}
			_currentText += ">";
		}

		return this;
	}

	public XMLPostContentHandleState endElement(String uri, String localName, String qName) throws SAXException
	{
		if (qName.equals("comments"))
			return _previousState;

		if (qName.equals("comment"))
		{
			_post.addComment(_commentUnderConstruction);
			//postManager.addComment(_post, _commentUnderConstruction);
			_commentUnderConstruction = new Comment();
			return this;
		}
		if (qName.equals("commentText"))
		{
			_commentUnderConstruction.setText(_currentText,false);
			_currentText = new String();
			return this;
		}
		if (qName.equals("commentDate"))
		{
			_commentUnderConstruction.setCreatedDate(new Date(Long.parseLong(_currentText)));
			// _commentUnderConstruction.setDate(Long.parseLong(_currentText));
			_currentText = new String();
			return this;
		}
		if (!isKnowTag(qName))
		{
			// Non expected tags, probabily they are html tags. We will add them to the text
			_currentText += "</" + qName + ">";
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

	private boolean isKnowTag(String tag)
	{
		if (tag.equals("comments") || tag.equals("comment") || tag.equals("commentText") || tag.equals("commentDate") || tag.equals("commentCreator"))
			return true;
		return false;
	}
}
