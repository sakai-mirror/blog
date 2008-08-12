/*************************************************************************************
 * Copyright 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.

 *************************************************************************************/
package org.sakaiproject.blog.api.datamodel;

import org.sakaiproject.blog.api.util.JpegTransformer;
import org.sakaiproject.blog.api.util.JpegTransformerException;

public class Image extends PostElement
{
	public static final String IMAGE = "IMAGE";

	public static final int ALL = 0;

	public static final int WEB = 2;

	public static final int ORIGINAL = 3;

	public static final int NONE = 4;

	private String fileName;

	transient private byte[] content;

	transient private byte[] websize;

	private String fullResourceId;

	private String webResourceId;

	private boolean contentChanged;

	public Image()
	{
		super();
	}

	public Image(String fileName, byte[] content)
	{
		super();
		this.fileName = fileName;
		this.content = content;
	}

	public void setFullContent(byte[] content)
	{
		this.content = content;
		try
		{
			JpegTransformer transformer = new JpegTransformer(content);
			setImageContentWithWebSize(transformer.transformJpegFixingLongestDimension(300, 0.8f));
		}
		catch (JpegTransformerException e)
		{
			e.printStackTrace();
		}
		contentChanged = true;
	}

	public byte[] getFullContent()
	{
		return content;
	}

	public void setImageContentWithWebSize(byte[] websize)
	{
		this.websize = websize;
	}

	public byte[] getImageContentWithWebSize()
	{
		return websize;
	}

	public void setFullResourceId(String fullResourceId)
	{
		this.fullResourceId = fullResourceId;
	}

	public String getFullResourceId()
	{
		return fullResourceId;
	}

	public void setWebResourceId(String webResourceId)
	{
		this.webResourceId = webResourceId;
	}

	public String getWebResourceId()
	{
		return webResourceId;
	}

	public String getType()
	{
		return IMAGE;
	}

	public boolean equals(Object o)
	{
		if (!(o instanceof Image))
			return false;

		Image image = (Image) o;

		if (!image.getId().equals(this.getId()))
			return false;
		
		if(!image.getFileName().equals(this.getFileName()))
			return false;

		if (!image.getFullResourceId().equals(this.getFullResourceId()))
			return false;

		if (!image.getWebResourceId().equals(this.getWebResourceId()))
			return false;

		return true;

	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setContentChanged(boolean contentChanged)
	{
		this.contentChanged = contentChanged;
	}

	public boolean isContentChanged()
	{
		return contentChanged;
	}

}
