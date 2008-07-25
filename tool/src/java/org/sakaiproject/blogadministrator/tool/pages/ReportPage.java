package org.sakaiproject.blogadministrator.tool.pages;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.WebResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.resource.ByteArrayResource;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.sakaiproject.wicket.markup.html.SakaiPortletWebPage;

public class ReportPage extends SakaiPortletWebPage
{
	private transient Logger logger = Logger.getLogger(ReportPage.class);
	
	public ReportPage(final String report)
	{
		super();
		
		if(logger.isDebugEnabled()) logger.debug("ReportPage()");
		
		WebResource resource = new WebResource()
		{
			@Override
			public IResourceStream getResourceStream()
			{
				return new AbstractResourceStream()
				{

					public void close() throws IOException
					{
					}

					public InputStream getInputStream() throws ResourceStreamNotFoundException
					{
						return new ByteArrayInputStream(report.getBytes());
					}
				};
			}

			@Override
			public void setHeaders(WebResponse response)
			{
				response.setContentType("text/plain");
				response.setAttachmentHeader("blog-data-migration-report.txt");
			}
		};
		
		add(new ResourceLink("reportLink",resource));
	}
}
