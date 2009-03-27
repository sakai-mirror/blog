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

import java.io.Serializable;

public class State implements Serializable
{
	public static final String PRIVATE = "PRIVATE";

	public static final String READY = "READY";

	public static final String PUBLIC = "PUBLIC";
	
	public static final String RECYCLED = "RECYCLED";

	private String visibility;

	private boolean readOnly = true;

	private boolean allowComments = true;

	public State()
	{
		visibility = PRIVATE;
	}

	public boolean isPrivate()
	{
		return visibility.equals(PRIVATE);
	}

	public boolean isPublic()
	{
		return visibility.equals(PUBLIC);
	}

	public String getVisibility()
	{
		return visibility;
	}

	public void setVisibility(String visibility)
	{
		this.visibility = visibility;
	}

	public void setReadOnly(boolean readOnly)
	{
		this.readOnly = readOnly;
	}

	public boolean isReadOnly()
	{
		return this.readOnly;
	}

	public void setAllowComments(boolean allowComments)
	{
		this.allowComments = allowComments;
	}

	public boolean isAllowComments()
	{
		return allowComments;
	}

	public boolean isRecycled()
	{
		return visibility.equals(RECYCLED);
	}

	public boolean isReady()
	{
		return visibility.equals(READY);
	}
}
