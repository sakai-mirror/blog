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

package org.sakaiproject.tool.blog.api;

import java.io.Serializable;

public class QueryBean implements Serializable
{
	private String _queryString;

	private String[] visibilities;

	private long _initDate;

	private long _endDate;

	private String _user;

	private String siteId;
	
	private String caller;

	public QueryBean()
	{
		visibilities = new String[] {}; // this mean no filter by visibility
		_initDate = -1; // this mean no filter by initDate;
		_endDate = -1; // this mean no filter by endDate
		_user = "";
		siteId = "";
		caller = "";
	}

	public boolean hasConditions()
	{
		return siteId.length() > 0 || visibilities.length > 0 || _initDate != -1 || _endDate != -1;
	}

	public void setQueryString(String queryString)
	{
		_queryString = queryString;
	}

	public String getQueryString()
	{
		return _queryString;
	}

	public boolean queryBySiteId()
	{
		return !siteId.equals("");
	}

	public boolean queryByVisibility()
	{
		return visibilities.length > 0;
	}

	public void setVisibilities(String[] visibilities)
	{
		this.visibilities = visibilities;
	}

	public String[] getVisibilities()
	{
		return visibilities;
	}

	public boolean queryByInitDate()
	{
		return _initDate != -1;
	}

	public void setInitDate(long initDate)
	{
		_initDate = initDate;
	}

	public long getInitDate()
	{
		return _initDate;
	}

	public boolean queryByEndDate()
	{
		return _endDate != -1;
	}

	public void setEndDate(long endDate)
	{
		_endDate = endDate;
	}

	public long getEndDate()
	{
		return _endDate;
	}

	public void setCreator(String user)
	{
		this._user = user;
	}

	public String getCreator()
	{
		return _user;
	}

	public void setSiteId(String siteId)
	{
		this.siteId = siteId;
	}

	public String getSiteId()
	{
		return siteId;
	}

	public boolean queryByCreator()
	{
		return ! _user.trim().equals("");
	}

	public void setCaller(String caller)
	{
		this.caller = caller;
	}

	public String getCaller()
	{
		return caller;
	}
}
