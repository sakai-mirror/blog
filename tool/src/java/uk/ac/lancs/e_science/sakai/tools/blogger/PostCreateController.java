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

package uk.ac.lancs.e_science.sakai.tools.blogger;

import javax.faces.context.FacesContext;

import javax.servlet.http.HttpSession;


import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.BloggerManager;

import java.util.Map;


public class PostCreateController extends PostEditionAbstractController{

	public PostCreateController(){
    	blogger = BloggerManager.getBlogger();
    }
    
    public String newPost(){
        post = new Post();
        HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        session.setAttribute("post",post);
        return "newPost";
    }
    public String doSave(){
    	super.doSave();
        return "main";
    }   
    public String doPreview(){
        Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        sessionMap.put("back","newPost");    	
    	return super.doPreview();
    }
    public String addParagraph(){
    	super.addParagraph();
    	return "refreshNewPost";
    }
    
    public String modifyParagraph(){
    	super.modifyParagraph();
    	return "refreshNewPost";
    }    
    
    public String addImage(){
    	super.addImage();
    	return "refreshNewPost";
    }
    
    public String modifyImage(){
    	super.modifyImage();
    	return "refreshNewPost";    	
    }
    public String setCurrentElementIndex(int currentElementIndex) {
    	super.setCurrentElementIndex(currentElementIndex);
    	return "refreshNewPost";    	
    	
    }
    
        
}
