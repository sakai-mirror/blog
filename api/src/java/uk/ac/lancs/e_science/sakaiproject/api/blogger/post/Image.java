/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.lancs.e_science.sakaiproject.api.blogger.post;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.util.UIDGenerator;

public class Image extends PostElement{
	private String idImage;
	private String description;
	private byte[] content;
	private byte[] thumbnail;
	private byte[] websize;
	
	public Image(){
		this.idImage = UIDGenerator.getIdentifier(this); //automatically, we put an oid
	}
	public Image(String description, byte[] content){
		this();
		this.description = description;
		this.content = content;
	}
	public void setIdImage(String idImage){
		this.idImage = idImage;
		
	}
	public String getIdImage(){
		return idImage;
	}
	public void setContent(byte[] content){
		this.content = content;
	}
	public byte[] getContent(){
		return content;
	}
	public void setThumbnail(byte[] thumbnail){
		this.thumbnail = thumbnail;
	}
	public byte[] getThumbnail(){
		return thumbnail;
	}	
	public void setWebsize(byte[] websize){
		this.websize = websize;
	}
	public byte[] getWebsize(){
		return websize;
	}	
	public void setDescription(String description){
		this.description = description;
	}
	public String getDescription(){
		return description;
	}
		
}
