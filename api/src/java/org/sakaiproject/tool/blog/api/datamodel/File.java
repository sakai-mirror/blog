/*************************************************************************************
 Copyright (c) 2006. Centre for e-Science. Lancaster University. United Kingdom.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 *************************************************************************************/

package org.sakaiproject.tool.blog.api.datamodel;

public class File extends PostElement
{
	public static final String FILE = "FILE";

	private transient byte[] content;

	private String mimeType;

	private String resourceId;
	
	private String fileName;

	private boolean contentChanged;

	public File()
	{
		super();
	}
	
	public File(File from)
	{
		super(from.getId(),from.getDisplayName());
		
		mimeType = from.getMimeType();
		content = from.getContent();
		resourceId = from.getResourceId();
		fileName = from.getFileName();
	}

	public File(String id, String fileName, String mimeType, byte[] content)
	{
		super(id);

		this.fileName = fileName;
		this.mimeType = mimeType;
		this.content = content;
	}

	public void setContent(byte[] content)
	{
		this.content = content;
		contentChanged = true;
	}

	public byte[] getContent()
	{
		return content;
	}

	public void setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
	}

	public String getMimeType()
	{
		return mimeType;
	}

	public void setResourceId(String resourceId)
	{
		this.resourceId = resourceId;
	}

	public String getResourceId()
	{
		return resourceId;
	}

	public String getType()
	{
		return FILE;
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
