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
package uk.ac.lancs.e_science.sakaiproject.api.blogger;

import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;

public class SakaiProxy {
    private static ToolManager toolManager = org.sakaiproject.tool.cover.ToolManager.getInstance();
    private static SessionManager sessionManager = org.sakaiproject.tool.cover.SessionManager.getInstance();
    
	public static String getCurrentSiteId(){
		return toolManager.getCurrentPlacement().getContext(); //equivalent to PortalService.getCurrentSiteId();
	}
	public static String getCurretUserEid(){
		
		return sessionManager.getCurrentSession().getUserEid();
	}
    public static String getDiplayNameForTheUser(String userEid){
    	try{
    		User sakaiUser = UserDirectoryService.getInstance().getUserByEid(userEid);
    		return sakaiUser.getDisplayName();
    	} catch (Exception e){
    		return userEid; //this can happen if the user does not longer exist in the system
    	}
    }

}
