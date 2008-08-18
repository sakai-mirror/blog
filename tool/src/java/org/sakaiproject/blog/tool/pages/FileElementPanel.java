package org.sakaiproject.blog.tool.pages;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.sakaiproject.blog.tool.markup.html.image.ExternalImage;

public class FileElementPanel extends Panel
{
	public FileElementPanel(String id, ExternalLink link, String iconUrl)
	{
        super( id);
        
        init(id,link,iconUrl);
		
	}
	public FileElementPanel(String id, ResourceLink link, String iconUrl)
	{
        super( id);
        
        init(id,link,iconUrl);
    }
	
	private void init(String id,Component link,String iconUrl)
	{
        ExternalImage img = new ExternalImage( "icon", iconUrl);
        add(img);
        add( link);
	}

	
}