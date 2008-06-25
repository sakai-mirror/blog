package org.sakaiproject.tool.blog.markup.html.image;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.image.Image;

public class ExternalImage extends Image
{
	private String url;
	
	public ExternalImage(String id,String url)
	{
		super(id);
		
		this.url = url;
	}
	
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.put("src", url);
	}
}
