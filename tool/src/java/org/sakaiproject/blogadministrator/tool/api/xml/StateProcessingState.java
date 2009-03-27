package org.sakaiproject.blogadministrator.tool.api.xml;

import org.sakaiproject.blog.api.datamodel.State;
import org.sakaiproject.blog.api.datamodel.Post;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class StateProcessingState extends XMLPostContentHandleState
{
	private Post _post;

	private String _currentText;

	// private State _stateUnderConstruction;
	private XMLPostContentHandleState _previousState;

	public StateProcessingState(Post post, XMLPostContentHandleState previousState)
	{
		super();
		
		_currentText = new String();
		_previousState = previousState;
		_post = post;
		// _stateUnderConstruction = new State();
	}

	public XMLPostContentHandleState startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
	{
		return this;
	}

	public XMLPostContentHandleState endElement(String uri, String localName, String qName) throws SAXException
	{
       if (qName.equals("state")){
            //_post.setState(_stateUnderConstruction);
            return _previousState;
       }
		if (qName.equals("visibility"))
		{
			int visibility = Integer.parseInt(_currentText);
			
			switch(visibility)
			{
				case 0:
				{
					_post.setVisibility(State.PRIVATE);
					break;
				}
				case 1:
				{
					_post.setVisibility(State.READY);
					break;
				}
				case 2:
				{
					_post.setVisibility(State.PUBLIC);
					break;
				}
				case 3:
				{
					_post.setVisibility(State.PRIVATE);
					break;
				}
				default:
					System.out.println("Unrecognized post visibility");
			}
			
			//postManager.saveVisibility(_post);
			
			_currentText = new String();
			return this;
		}
		if (qName.equals("readOnly"))
		{
			_post.setReadOnly(parseBoolean(_currentText));
			//postManager.saveReadOnly(_post);
			_currentText = new String();
			return this;
		}
		if (qName.equals("allowComments"))
		{
			_post.setCommentable(parseBoolean(_currentText));
			//postManager.saveAllowComments(_post);
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

	private boolean parseBoolean(String parseString)
	{
		if ("true".equals(parseString))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
