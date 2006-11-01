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

package uk.ac.lancs.e_science.sakai.tools.blogger.cacheForImages;

import java.util.Date;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Image;

public class CacheForImagesEntry {
	private Date lastAccess;
	private Image image;
	
	public CacheForImagesEntry(Image image){
		this.image = image;
		lastAccess=new Date();
	}
	
	public void updateLastAccess(){
		lastAccess=new Date();
	}
	public long getMilliseconds(){
		return lastAccess.getTime();
	}
	public Image getImage(){
		return image;
	}
}
