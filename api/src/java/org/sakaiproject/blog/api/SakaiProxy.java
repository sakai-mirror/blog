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

	public String getPageId();
	
	public boolean isMaintainer(String userId);
	
    public boolean isMaintainer(String userId,String siteId);

	public List<BlogMember> getNonMaintainerSiteMembers();
	
	public List<String> getIdMaintainerSiteMembers();
    
    public List<String> getIdMaintainerSiteMembers(String siteId);

	public boolean isCurrentUserMaintainer();
	
	public boolean isCurrentUserAdmin();

	public String getSakaiProperty(String string);

	public BlogMember getMember(String memberId);

	public void setToolManager(ToolManager toolManager);

	public ToolManager getToolManager();

	public void setSessionManager(SessionManager sessionManager);

	public SessionManager getSessionManager();

	public void setAuthzGroupService(AuthzGroupService authzGroupService);

	public AuthzGroupService getAuthzGroupService();

	public String getCurrentSiteCollectionId();
	
	public boolean isAutoDDL();

	public List<BlogMember> getSiteMembers();
	
	public boolean fileExists(String resourceId) throws Exception;
	
	public String saveFile(Post post,String name,String mimeType, byte[] fileData);
	
	public void getFile(File file);
	
	public void getImage(Image image,int size);
	
	public String getPortalUrl();
	
	public String getAccessUrl();

	public void setContentHostingService(ContentHostingService contentHostingService);

	public ContentHostingService getContentHostingService();

	public void setSiteService(SiteService siteService);

	public SiteService getSiteService();

	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService);

	public ServerConfigurationService getServerConfigurationService();

	public void setAuthenticationManager(AuthenticationManager authenticationManager);

	public AuthenticationManager getAuthenticationManager();

	public void setUserDirectoryService(UserDirectoryService userDirectoryService);

	public UserDirectoryService getUserDirectoryService();

	public void registerEntityProducer(EntityProducer entityProducer);

	public void setEntityManager(EntityManager entityManager);

	public EntityManager getEntityManager();

	public void deleteResources(String[] resourceIds);

	public Profile getUserProfileById(String userId);

	public void setProfileManager(ProfileManager profileManager);

	public ProfileManager getProfileManager();
	
	public void deleteFolderForPost(String postId);

	public void setSqlService(SqlService sqlService);

	public SqlService getSqlService();

	public String getResourceUrl(String resourceId);

	public boolean isOnGateway();

	public Profile getProfile(String userId);

	public void registerFunction(String function);

	public void setFunctionManager(FunctionManager functionManager);

	public FunctionManager getFunctionManager();

	public boolean isAllowedFunction(String function);

	public List<BlogPermissions> getPermissions();

	public void savePermissions(BlogPermissions permissions) throws Exception;

	public void addEventObserver(Observer observer);

	public void setEventTrackingService(EventTrackingService eventTrackingService);

	public EventTrackingService getEventTrackingService();

	public void deleteEventObserver(Observer observer);

	public String getIdForEid(String eid);

	public boolean siteExists(String siteId);

	public void allowFunction(String string, String blogPostCreate);

	public boolean currentSiteHasRole(String string);

    public Reference newReference(String reference);

	public void sendEmailWithMessage(Set<String> emails, String subject,String string);

	public Set<String> getSiteUsers();

	public void sendEmailWithMessage(String creatorId, String subject, String string);
}
