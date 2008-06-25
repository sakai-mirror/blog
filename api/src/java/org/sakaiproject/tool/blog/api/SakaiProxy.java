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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Observer;
import java.util.Set;

import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.tool.blog.api.datamodel.BlogPermissions;
import org.sakaiproject.tool.blog.api.datamodel.File;
import org.sakaiproject.tool.blog.api.datamodel.Image;
import org.sakaiproject.tool.blog.api.datamodel.Post;

public interface SakaiProxy
{
	public String getCurrentSiteId();

	public String getCurrentUserId();
	
	public String getCurrentUserEid();

	public String getDisplayNameForTheUser(String userId);

	public String getEmailForTheUser(String userEid);

	public String getPageId();

	public boolean isMaintainer(String userId);

	public List<BlogMember> getNonMaintainerSiteMembers();

	public List<BlogMember> getSiteMembers();

	//public List<String> getEidMaintainerSiteMembers();

	public boolean isCurrentUserMaintainer();
	
	public BlogMember getMember(String memberId);
	
	public String getCurrentSiteCollectionId();

	public String getPortalUrl();
	
	public boolean isAutoDDL();
	
	public String getVendor();

	public String getAccessUrl();
	
	public boolean fileExists(String resourceId) throws Exception;
	
	public String saveFile(Post post,String name,String mimeType,byte[] fileData);

	public void registerEntityProducer(EntityProducer entityProducer);

	public boolean isCurrentUserAdmin();
	
	public void getFile(File file);
	public void getImage(Image image,int size);

	public void deleteResources(String[] resourceIds);
	
	public Profile getUserProfileById(String userId);

	public void deleteFolderForPost(String id);
	
	public void returnConnection(Connection connection);

	public Connection borrowConnection() throws SQLException;

	public String getResourceUrl(String fullResourceId);

	public boolean isOnGateway();

	public Profile getProfile(String userId);
	
	public void registerFunction(String function);

	public boolean isAllowedFunction(String blogCommentDelete);

	public List<BlogPermissions> getPermissions();

	public void savePermissions(BlogPermissions permissions) throws Exception;
	
	public void addEventObserver(Observer observer);

	public void deleteEventObserver(Observer observer);
	
	public String getIdForEid(String eid);
	
	public boolean siteExists(String siteId);

	public boolean currentSiteHasRole(String string);

	public void allowFunction(String string, String blogPostCreate);
}
