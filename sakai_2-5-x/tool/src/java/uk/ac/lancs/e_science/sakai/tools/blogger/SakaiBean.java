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

package uk.ac.lancs.e_science.sakai.tools.blogger;

import java.util.Collections;
import java.util.List;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.Member;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.SakaiProxy;




public class SakaiBean extends BloggerController{
    public SakaiBean(){
    	
    }
    public boolean isGatewaySite(){
    	System.out.println(SakaiProxy.getCurrentSiteId());
    	if (SakaiProxy.getCurrentSiteId().equals("!Gateway")){
    		return true;
    	}
    	return false;
    }
    public String getSite(){
    	return SakaiProxy.getCurrentSiteId();
    }    
    public List<Member> getNonMaintainerSiteMembers(){
    	List<Member> result = SakaiProxy.getNonMaintainerSiteMembers();
    	Collections.sort(result, new MemberComparator());
    	return result; 
    }
    public List<Member> getSiteMembers(){
    	List<Member> result = SakaiProxy.getSiteMembers();
    	Collections.sort(result, new MemberComparator());
    	return result; 
    }    
    
    public String getCurrentUserEid(){
    	return SakaiProxy.getCurrentUserId();
    }
    public boolean isCurrentUserMaintainer(){
    	return SakaiProxy.isCurrentUserMaintainer();
    }
    public boolean getNotCurrentUserMaintainer(){
    	boolean result = !SakaiProxy.isCurrentUserMaintainer(); 
    	return result;
    }    
}
