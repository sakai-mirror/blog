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

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class Comment implements Serializable{
	private String id;
    private String text;
    private Date createdDate;
    private Date modifiedDate;
    private String creatorId;
    private String postId;

    public Comment()
    {
    	this("");
    }
    public Comment(String text){
        this.text = text;
        id = UUID.randomUUID().toString();
        createdDate = new Date();
        modifiedDate = createdDate;
    }
    public Comment(String text, Date createdDate)
    {
        this.text = text;
        this.createdDate = createdDate;
    }
    
    /**
     * If the supplied is different to the current, sets the modified date to
     * the current date so ... be careful!
     * 
     * @param text
     */
    public void setText(String text)
    {
    	setText(text,true);
    }
    
    public void setText(String text,boolean modified)
    {
    	if(!this.text.equals(text) && modified)
    		modifiedDate = new Date();
    	
        this.text = text;
    }
    public String getText(){
        return text;
    }
    public String getCreatorId(){
        return creatorId;
    }
    public void setCreatorId(String creatorId){
        this.creatorId = creatorId;
    }
	public void setCreatedDate(Date createdDate)
	{
		this.createdDate = createdDate;
		this.modifiedDate = createdDate;
	}
	public Date getCreatedDate()
	{
		return createdDate;
	}
	public void setModifiedDate(Date modifiedDate)
	{
		this.modifiedDate = modifiedDate;
	}
	public Date getModifiedDate()
	{
		return modifiedDate;
	}
	public void setId(String id)
	{
		this.id = id;
	}
	public String getId()
	{
		return id;
	}
	public void setPostId(String postId)
	{
		this.postId = postId;
	}
	public String getPostId()
	{
		return postId;
	}
}
