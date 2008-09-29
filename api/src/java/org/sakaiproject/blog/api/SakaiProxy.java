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
package org.sakaiproject.blog.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Observer;
import java.util.Set;

import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.profile.ProfileManager;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blog.api.datamodel.BlogPermissions;
import org.sakaiproject.blog.api.datamodel.File;
import org.sakaiproject.blog.api.datamodel.Image;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.blog.api.BlogMember;

public interface SakaiProxy
{
	public String getCurrentSiteId();

	public String getCurrentUserId();
	
	public Connection borrowConnection() throws SQLException;
	
	public void returnConnection(Connection connection);
	
	public String getCurrentUserEid();

	public String getCurrentUserDisplayName();
	
	public String getVendor();

	public String getDisplayNameForTheUser(String userId);

	public String getEmailForTheUser(String userId);
	
	public boolean isMaintainer(String userId);
	
    public boolean isMaintainer(String userId,String siteId);

	public List<BlogMember> getNonMaintainerSiteMembers();
	
	public List<String> getIdMaintainerSiteMembers();
    
    public List<String> getIdMaintainerSiteMembers(String siteId);

	public boolean isCurrentUserMaintainer();
	
	public boolean isCurrentUserAdmin();

	public String getSakaiProperty(String string);

	public BlogMember getMember(String memberId);

	public String getCurrentSiteCollectionId();
	
	public boolean isAutoDDL();

	public List<BlogMember> getSiteMembers();
	
	public boolean fileExists(String resourceId) throws Exception;
	
	public String saveFile(Post post,String name,String mimeType, byte[] fileData);
	
	public void getFile(File file);
	
	public void getImage(Image image,int size);
	
	public String getPortalUrl();
	
	public String getAccessUrl();

	public void registerEntityProducer(EntityProducer entityProducer);

	public void deleteResources(String[] resourceIds);

	public Profile getUserProfileById(String userId);

	public void deleteFolderForPost(String postId);

	public String getResourceUrl(String resourceId);

	public boolean isOnGateway();

	public Profile getProfile(String userId);

	public void registerFunction(String function);

	public boolean isAllowedFunction(String function);

	public List<BlogPermissions> getPermissions();

	public void savePermissions(BlogPermissions permissions) throws Exception;

	public void addEventObserver(Observer observer);

	public void deleteEventObserver(Observer observer);

	public String getIdForEid(String eid);

	public boolean siteExists(String siteId);

	public void allowFunction(String string, String blogPostCreate);

	public boolean currentSiteHasRole(String string);

    public Reference newReference(String reference);

	public void sendEmailWithMessage(Set<String> emails, String subject,String string);

	public Set<String> getSiteUsers();

	public void sendEmailWithMessage(String creatorId, String subject, String string);

	public void registerSecurityAdvisor(SecurityAdvisor securityAdvisor);

	/**
	 * Returns the user ids of users in the Tutor role
	 */
	public Set<String> getTutors();

	public boolean isCurrentUserTutor();

	/**
	 * Returns the user ids of users in the Tutor role, for the specified site
	 */
	public Set<String> getTutors(String siteId);

	public String getCurrentPageId();
	
	public String getCurrentToolId();
}
