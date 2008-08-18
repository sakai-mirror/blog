package org.sakaiproject.blog.api.datamodel;

import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.sakaiproject.blog.api.BlogManager;
import org.sakaiproject.blog.api.XmlDefs;
import org.sakaiproject.blog.cover.SakaiProxy;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.util.BaseResourceProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Post implements Entity, Serializable
{
	private static final String CDATA_SUFFIX = "]]>";
	private static final String CDATA_PREFIX = "<![CDATA[";
		
	private String id = "";

	private String title = "";

	private String shortText = "";

	private Date createdDate;
	
	private Date modifiedDate;

	private State state = new State();

	private String creatorId = null;

	private String keywords = "";

	private List<PostElement> elements = new ArrayList<PostElement>(); // PostElement

	private List<Comment> comments = new ArrayList<Comment>();

	private String siteId;

	private boolean dirty = true;

	public Post()
	{
		id = UUID.randomUUID().toString();
		createdDate = new Date();
		modifiedDate = new Date();
	}

	public void setId(String oid)
	{
		this.id = oid;
	}
	
	public void addKeyword(String keyword)
	{
		keywords += keyword;
	}

	/**
	 * @see org.sakaiproject.entity.api.Entity#getId()
	 */
	public String getId()
	{
		return id;
	}

	public void setTitle(String title)
	{
		this.title = title;
		dirty = true;
	}

	public String getTitle()
	{
		return title;
	}

	public void setShortText(String shortText)
	{
		if (shortText != null)
		{
			this.shortText = StringEscapeUtils.unescapeHtml(shortText.trim());
			dirty = true;
		}
	}

	public String getShortText()
	{
		return shortText;
	}

	public void setCreatorId(String creatorId)
	{
		this.creatorId = creatorId;
		dirty = true;
	}

	public String getCreatorId()
	{
		return creatorId;
	}

	public String getKeywords()
	{
		return keywords;
	}

	public void setKeywords(String keywords)
	{
		this.keywords = keywords;
		
		if(this.keywords == null)
			this.keywords = "";
		
		dirty = true;
	}

	public void addElement(PostElement element)
	{
		elements.add(element);
		dirty = true;
	}
	
	public void addElement(PostElement element, int index)
	{
		elements.add(index,element);
		dirty = true;
	}

	public void replaceElement(PostElement newElement, int index)
	{
		elements.set(index,newElement);
		dirty = true;
	}

	public void addComment(Comment comment)
	{
		comment.setPostId(id);
		comments.add(comment);
		dirty = true;
	}

	public List<Comment> getComments()
	{
		return comments;
	}

	public List<Image> getImages()
	{
		List<Image> result = new ArrayList<Image>();
		
		for(PostElement element : elements)
		{
			if (element instanceof Image)
				result.add((Image) element);
		}
		
		return result;

	}
	
	public List<Paragraph> getParagraphs()
	{
		List<Paragraph> result = new ArrayList<Paragraph>();
		
		for(PostElement element : elements)
		{
			if (element instanceof Paragraph)
				result.add((Paragraph) element);
		}
		
		return result;
	}
	
	public List<LinkRule> getLinks()
	{
		List<LinkRule> result = new ArrayList<LinkRule>();
		
		for(PostElement element : elements)
		{
			if (element instanceof LinkRule)
				result.add((LinkRule)element);
		}
		
		return result;
	}

	public List<File> getFiles()
	{
		List<File> result = new ArrayList<File>();
		for (PostElement element : elements)
		{
			if (element instanceof File)
				result.add((File) element);
		}
		
		return result;
	}

	public Iterator<PostElement> getElements()
	{
		return elements.iterator();
	}
	
	public int size() { return elements.size(); }
	
	public void removeElement(PostElement element)
	{
		removeElement(getElementPosition(element));
	}

	public void removeElement(int index)
	{
		elements.remove(index);
		dirty = true;
	}

	public boolean hasImage(Image test)
	{
		for (Image image : getImages())
		{
			if (image.equals(test))
				return true;
		}
		
		return false;
	}

	public boolean hasFile(String fileId)
	{
		for(File file : getFiles())
		{
			if(file.getId().equals(fileId.trim()))
				return true;
		}
		
		return false;
	}

	public void setSiteId(String siteId)
	{
		this.siteId = siteId;
		dirty = true;
	}

	public String getSiteId() { return siteId; }

	public void setDirty(boolean dirty) { this.dirty = dirty; }

	public boolean isDirty() { return dirty; }

	public boolean isReadOnly() { return state.isReadOnly(); }

	public boolean isCommentable() { return state.isAllowComments(); }

	public void setVisibility(String visibility)
	{
		state.setVisibility(visibility);
		dirty = true;
	}

	public String getVisibility() { return state.getVisibility(); }

	public void setReadOnly(boolean b)
	{
		state.setReadOnly(b);
		dirty = true;
	}

	public void setCommentable(boolean b)
	{
		state.setAllowComments(b);
		dirty = true;
	}

	public boolean isPrivate() { return state.isPrivate(); }

	public boolean isPublic() { return state.isPublic(); }

	/**
	 * @see org.sakaiproject.entity.api.Entity#getProperties()
	 */
	public ResourceProperties getProperties()
	{
		ResourceProperties rp = new BaseResourceProperties();
		
		rp.addProperty("id", getId());
		return rp;
	}

	/**
	 * @see org.sakaiproject.entity.api.Entity#getReference()
	 * 
	 * @return
	 */
	public String getReference()
	{
		return BlogManager.REFERENCE_ROOT + Entity.SEPARATOR + "post" + Entity.SEPARATOR + id;
	}

	/**
	 * @see org.sakaiproject.entity.api.Entity#getReference()
	 * 
	 * @return
	 */
	public String getReference(String rootProperty)
	{
		return getReference();
	}

	/**
	 * @see org.sakaiproject.entity.api.Entity#getUrl()
	 */
	public String getUrl()
	{
		String url = SakaiProxy.getAccessUrl();
		url += getReference();
		
		return url;
	}

	/**
	 * @see org.sakaiproject.entity.api.Entity#getUrl()
	 */
	public String getUrl(String rootProperty)
	{
		return getUrl();
	}

	/**
	 * @see org.sakaiproject.entity.api.Entity#toXml()
	 * 
	 * @return
	 */
	public Element toXml(Document doc, Stack stack)
	{
		Element postElement = doc.createElement(XmlDefs.POST);

		if (stack.isEmpty())
		{
			doc.appendChild(postElement);
		}
		else
		{
			((Element) stack.peek()).appendChild(postElement);
		}

		stack.push(postElement);

		postElement.setAttribute(XmlDefs.READONLY, ((isReadOnly()) ? "true" : "false"));
		postElement.setAttribute(XmlDefs.COMMENTABLE, ((isCommentable()) ? "true" : "false"));
		postElement.setAttribute(XmlDefs.VISIBILITY, getVisibility());

		Element idElement = doc.createElement(XmlDefs.ID);
		idElement.setTextContent(id);
		postElement.appendChild(idElement);

		Element createdDateElement = doc.createElement(XmlDefs.CREATEDDATE);
		createdDateElement.setTextContent(Long.toString(createdDate.getTime()));
		postElement.appendChild(createdDateElement);
		
		Element modifiedDateElement = doc.createElement(XmlDefs.MODIFIEDDATE);
		modifiedDateElement.setTextContent(Long.toString(modifiedDate.getTime()));
		postElement.appendChild(modifiedDateElement);

		Element creatorIdElement = doc.createElement(XmlDefs.CREATORID);
		creatorIdElement.setTextContent(creatorId);
		postElement.appendChild(creatorIdElement);

		Element keywordsElement = doc.createElement(XmlDefs.KEYWORDS);
		keywordsElement.setTextContent(wrapWithCDATA(keywords));
		postElement.appendChild(keywordsElement);

		Element titleElement = doc.createElement(XmlDefs.TITLE);
		titleElement.setTextContent(wrapWithCDATA(title));
		postElement.appendChild(titleElement);

		Element shortTextElement = doc.createElement(XmlDefs.SHORTTEXT);
		shortTextElement.setTextContent(wrapWithCDATA(shortText));
		postElement.appendChild(shortTextElement);
		
		if(comments.size() > 0)
		{
			Element commentsElement = doc.createElement(XmlDefs.COMMENTS);
			
			for(Comment comment: comments)
			{
				Element commentElement = doc.createElement(XmlDefs.COMMENT);
				commentElement.setAttribute(XmlDefs.ID, comment.getId());
				commentElement.setAttribute(XmlDefs.CREATORID, comment.getCreatorId());
				commentElement.setAttribute(XmlDefs.CREATEDDATE, Long.toString(comment.getCreatedDate().getTime()));
				commentElement.setAttribute(XmlDefs.MODIFIEDDATE, Long.toString(comment.getModifiedDate().getTime()));
				commentElement.setTextContent(wrapWithCDATA(comment.getText()));
				
				commentsElement.appendChild(commentElement);
			}
		
			postElement.appendChild(commentsElement);
		}
		
		for(PostElement element : elements)
		{
			if(element instanceof Image)
			{
				Image image = (Image) element;
				
				Element imageElement = doc.createElement(XmlDefs.IMAGE);
				imageElement.setAttribute(XmlDefs.ID, image.getId());
				imageElement.setAttribute(XmlDefs.FILENAME, image.getFileName());
				imageElement.setAttribute(XmlDefs.FULLRESOURCEID, image.getFullResourceId());
				imageElement.setAttribute(XmlDefs.WEBRESOURCEID, image.getWebResourceId());
				imageElement.setAttribute(XmlDefs.INDENTATION, Integer.toString(image.getIndentation()));
				
				postElement.appendChild(imageElement);
			}
			
			else if(element instanceof File)
			{
				File file = (File) element;
				Element fileElement = doc.createElement(XmlDefs.FILE);
				fileElement.setAttribute(XmlDefs.ID, file.getId());
				fileElement.setAttribute(XmlDefs.FILENAME, file.getFileName());
				fileElement.setAttribute(XmlDefs.RESOURCEID, file.getResourceId());
				fileElement.setAttribute(XmlDefs.DISPLAYNAME, file.getDisplayName());
				fileElement.setAttribute(XmlDefs.MIMETYPE, file.getMimeType());
				fileElement.setAttribute(XmlDefs.INDENTATION, Integer.toString(file.getIndentation()));
				
				postElement.appendChild(fileElement);
			}
			
			else if(element instanceof Paragraph)
			{
				Paragraph paragraph = (Paragraph) element;
				Element paragraphElement = doc.createElement(XmlDefs.PARAGRAPH);
				paragraphElement.setAttribute(XmlDefs.ID, paragraph.getId());
				paragraphElement.setAttribute(XmlDefs.INDENTATION, Integer.toString(paragraph.getIndentation()));
				
				Element paragraphElementText = doc.createElement(XmlDefs.TEXT);
				paragraphElementText.setTextContent(paragraph.getText());
				paragraphElement.appendChild(paragraphElementText);
				
				postElement.appendChild(paragraphElement);
			}
			
			else if(element instanceof LinkRule)
			{
				LinkRule link = (LinkRule) element;
				Element linkElement = doc.createElement(XmlDefs.LINK);
				linkElement.setAttribute(XmlDefs.ID, link.getId());
				linkElement.setAttribute(XmlDefs.INDENTATION, Integer.toString(link.getIndentation()));
				linkElement.setAttribute(XmlDefs.DISPLAYNAME, link.getDisplayName());
				linkElement.setAttribute(XmlDefs.URL, link.getUrl());
				
				postElement.appendChild(linkElement);
			}
		}

		stack.pop();

		return postElement;
	}
	
	private String wrapWithCDATA(String s)
	{
		return CDATA_PREFIX + s + CDATA_SUFFIX;
	}
	
	private String stripCDATA(String s)
	{
		if(s.startsWith(CDATA_PREFIX) && s.endsWith(CDATA_SUFFIX))
		{
			s = s.substring(CDATA_PREFIX.length());
			s = s.substring(0, s.length() - CDATA_SUFFIX.length());
		}
		
		return s;
	}

	public void fromXml(Element postElement)
	{
		if(!postElement.getTagName().equals(XmlDefs.POST))
		{
			return;
		}
		
		String readOnly = postElement.getAttribute(XmlDefs.READONLY);
		setReadOnly( (readOnly.equals("true")) ? true : false);
		
		String commentable = postElement.getAttribute(XmlDefs.COMMENTABLE);
		setCommentable( (commentable.equals("true")) ? true : false);
		
		String visibility = postElement.getAttribute(XmlDefs.VISIBILITY);
		setVisibility(visibility);
		
		NodeList children = postElement.getElementsByTagName(XmlDefs.CREATORID);
		setCreatorId(children.item(0).getFirstChild().getTextContent());
		
		children = postElement.getElementsByTagName(XmlDefs.CREATEDDATE);
		setCreatedDate(new Date(Long.parseLong(children.item(0).getFirstChild().getTextContent())));
		
		children = postElement.getElementsByTagName(XmlDefs.MODIFIEDDATE);
		setModifiedDate(new Date(Long.parseLong(children.item(0).getFirstChild().getTextContent())));
		
		children = postElement.getElementsByTagName(XmlDefs.TITLE);
		if(children.getLength() > 0)
		{	
			setTitle(stripCDATA(children.item(0).getFirstChild().getTextContent()));
		}
		
		children = postElement.getElementsByTagName(XmlDefs.SHORTTEXT);
		if(children.getLength() > 0)
		{	
			setShortText(stripCDATA(children.item(0).getFirstChild().getTextContent()));
		}
		
		children = postElement.getElementsByTagName(XmlDefs.KEYWORDS);
		if(children.getLength() > 0)
		{	
			setKeywords(stripCDATA(children.item(0).getFirstChild().getTextContent()));
		}
		
		children = postElement.getElementsByTagName(XmlDefs.PARAGRAPH);
		int numChildren = children.getLength();
		for(int i = 0; i < numChildren;i++)
		{
			Element paragraphElement = (Element) children.item(i);
			
			String indentation= paragraphElement.getAttribute(XmlDefs.INDENTATION);
			String text = paragraphElement.getFirstChild().getTextContent();
			
			Paragraph paragraph = new Paragraph();
			paragraph.setIndentation(Integer.parseInt(indentation));
			paragraph.setText(text);
			
			addElement(paragraph);
		}
		
		children = postElement.getElementsByTagName(XmlDefs.IMAGE);
		numChildren = children.getLength();
		for(int i = 0; i < numChildren;i++)
		{
			Element childElement = (Element) children.item(i);
			
			String indentation= childElement.getAttribute(XmlDefs.INDENTATION);
			
			String fileName = childElement.getAttribute(XmlDefs.FILENAME);
			String fullResourceId = childElement.getAttribute(XmlDefs.FULLRESOURCEID);
			String webResourceId = childElement.getAttribute(XmlDefs.WEBRESOURCEID);
			
			Image image = new Image();
			image.setIndentation(Integer.parseInt(indentation));
			image.setFileName(fileName);
			image.setFullResourceId(fullResourceId);
			image.setWebResourceId(webResourceId);
				
			addElement(image);
		}
		
		children = postElement.getElementsByTagName(XmlDefs.FILE);
		numChildren = children.getLength();
		for(int i = 0; i < numChildren;i++)
		{
			Element childElement = (Element) children.item(i);
			
			File file = new File();
			file.setIndentation(Integer.parseInt(childElement.getAttribute(XmlDefs.INDENTATION)));
			file.setFileName(childElement.getAttribute(XmlDefs.FILENAME));
			file.setResourceId(childElement.getAttribute(XmlDefs.RESOURCEID));
			file.setDisplayName(childElement.getAttribute(XmlDefs.DISPLAYNAME));
			file.setMimeType(childElement.getAttribute(XmlDefs.MIMETYPE));
				
			addElement(file);
		}
		
		children = postElement.getElementsByTagName(XmlDefs.LINK);
		numChildren = children.getLength();
		for(int i = 0; i < numChildren;i++)
		{
			Element childElement = (Element) children.item(i);
			
			String indentation= childElement.getAttribute(XmlDefs.INDENTATION);
			String displayName = childElement.getAttribute(XmlDefs.DISPLAYNAME);
			String url = childElement.getAttribute(XmlDefs.URL);
			
			LinkRule linkRule = new LinkRule();
			linkRule.setIndentation(Integer.parseInt(indentation));
			linkRule.setDisplayName(displayName);
			linkRule.setUrl(url);
				
			addElement(linkRule);
		}
		
		children = postElement.getElementsByTagName(XmlDefs.COMMENT);
		numChildren = children.getLength();
		for(int i = 0; i < numChildren;i++)
		{
			Element commentElement = (Element) children.item(i);
					
			String commentCreatorId = commentElement.getAttribute(XmlDefs.CREATORID);
			String commentCreatedDate = commentElement.getAttribute(XmlDefs.CREATEDDATE);
			String commentModifiedDate = commentElement.getAttribute(XmlDefs.MODIFIEDDATE);
			String text = commentElement.getFirstChild().getTextContent();
			
			Comment comment = new Comment();
			comment.setCreatorId(commentCreatorId);
			comment.setCreatedDate(new Date(Long.parseLong(commentCreatedDate)));
			comment.setModifiedDate(new Date(Long.parseLong(commentModifiedDate)));
			comment.setText(stripCDATA(text));
					
			addComment(comment);
		}
	}

	public void setCreatedDate(Date createdDate)
	{
		this.createdDate = createdDate;
	}

	public Date getCreatedDate() { return createdDate; }

	public void setModifiedDate(Date modifiedDate)
	{
		this.modifiedDate = modifiedDate;
	}

	public Date getModifiedDate() { return modifiedDate; }

	public PostElement getElement(int index)
	{
		return elements.get(index);
	}

	public boolean hasComments()
	{
		return comments.size() > 0;
	}

	public void removeComment(Comment comment)
	{
		comments.remove(comment);
		dirty = true;
	}

	public int getElementPosition(PostElement e)
	{
		return elements.indexOf(e);
	}

	public boolean isRecycled()
	{
		return state.isRecycled();
	}

	public boolean isReady()
	{
		return state.isReady();
	}
}
