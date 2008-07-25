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

package org.sakaiproject.blog.api.datamodel;

import java.io.Serializable;
import java.util.UUID;

public abstract class PostElement implements Serializable
{
	private String id;
	
	private String displayName;
	
	private int indentation = 0;
	
	public PostElement()
	{
		id = UUID.randomUUID().toString();
		displayName = "";
	}
	
	public PostElement(String id)
	{
		this.id = id;
		displayName = "";
	}
	
	public PostElement(String id,String displayName)
	{
		this.id = id;
		this.displayName = displayName;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getId()
	{
		return id;
	}
	
	public abstract String getType();

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public String getDisplayName()
	{
		return displayName;
	}
	
	public void setIndentation(int indentation)
	{
		this.indentation = indentation;
	}

	public int getIndentation()
	{
		return indentation;
	}
}
