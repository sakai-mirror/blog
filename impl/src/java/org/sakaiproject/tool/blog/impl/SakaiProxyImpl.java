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
package org.sakaiproject.tool.blog.impl;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observer;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.profile.ProfileManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.blog.api.BlogFunctions;
import org.sakaiproject.tool.blog.api.BlogMember;
import org.sakaiproject.tool.blog.api.SakaiProxy;
import org.sakaiproject.tool.blog.api.datamodel.BlogPermissions;
import org.sakaiproject.tool.blog.api.datamodel.File;
import org.sakaiproject.tool.blog.api.datamodel.Image;
import org.sakaiproject.tool.blog.api.datamodel.Post;
import org.sakaiproject.tool.blog.api.datamodel.State;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.BaseResourceProperties;

public class SakaiProxyImpl implements SakaiProxy
{
	private Logger logger = Logger.getLogger(SakaiProxyImpl.class);
	
	private ToolManager toolManager;

	private SessionManager sessionManager;

	private AuthzGroupService authzGroupService;

	private ServerConfigurationService serverConfigurationService;

	private SiteService siteService;

	private AuthenticationManager authenticationManager;
	
	private UserDirectoryService userDirectoryService;
	
	private ContentHostingService contentHostingService;
	
	private EntityManager entityManager;
	
	private ProfileManager profileManager;
	
	private SqlService sqlService;
	
	private FunctionManager functionManager;
	
	private EventTrackingService eventTrackingService;
	
	public void init() {}
	
	public void destroy() {}
	
	public String getCurrentSiteId()
	{
		return toolManager.getCurrentPlacement().getContext(); // equivalent to PortalService.getCurrentSiteId();
	}

	public String getCurrentUserId()
	{

		Session session = sessionManager.getCurrentSession();
		String userId = session.getUserId();
		return userId;
	}
	
	public Connection borrowConnection() throws SQLException
	{
		return sqlService.borrowConnection();
	}
	
	public void returnConnection(Connection connection)
	{
		sqlService.returnConnection(connection);
	}
	
	public String getCurrentUserEid(){
		
		Session session = sessionManager.getCurrentSession();
		String userEid = session.getUserEid();
		return userEid;
	}

	public String getCurrentUserDisplayName()
	{
		return getDisplayNameForTheUser(getCurrentUserId());
	}
	
	public String getVendor()
	{
		return sqlService.getVendor();
	}

	public String getDisplayNameForTheUser(String userId)
	{
		try
		{
			User sakaiUser = userDirectoryService.getUser(userId);
			//return sakaiUser.getFirstName() + " " + sakaiUser.getLastName();
			return sakaiUser.getDisplayName();
		}
		catch (Exception e)
		{
			return userId; // this can happen if the user does not longer exist in the system
		}
	}

	public String getEmailForTheUser(String userId)
	{
		try
		{
			User sakaiUser = userDirectoryService.getUser(userId);
			return sakaiUser.getEmail();
		}
		catch (Exception e)
		{
			return ""; // this can happen if the user does not longer exist in the system
		}

	}

	public String getPageId()
	{
		Placement placement = toolManager.getCurrentPlacement();

		return ((ToolConfiguration) placement).getPageId();

	}
	
	public boolean isMaintainer(String userId)
    {
    	return isMaintainer(userId, getCurrentSiteId());
    }
	
    public boolean isMaintainer(String userId,String siteId){
    	try{
    		if(userId == null || siteId == null)
    			return false;
    		Site site = siteService.getSite(siteId);
    		AuthzGroup realm = authzGroupService.getAuthzGroup(site.getReference());
    		User sakaiUser = userDirectoryService.getUser(userId);
    		Role r = realm.getUserRole(sakaiUser.getId());
    		if(r.getId().equals(realm.getMaintainRole())) // This bit could be wrong
    		{
    			return true;
    		} else {
    			return false;
    		}
		
    		
    	} catch (Exception e){
    		e.printStackTrace();
    		return false;
    	} 
    }

	public List<BlogMember> getNonMaintainerSiteMembers()
	{
		ArrayList<BlogMember> result = new ArrayList<BlogMember>();
		try
		{
			Site site = siteService.getSite(getCurrentSiteId());
			Set<User> users = site.getUsers();
			for (User sakaiUser : users)
			{
				if (!isMaintainer(sakaiUser.getId()))
				{
					BlogMember member = new BlogMember(sakaiUser);
																								// this
					result.add(member);
				}
			}
			return result;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return result;
		}
	}
	
	public List<String> getIdMaintainerSiteMembers(){
    	return getIdMaintainerSiteMembers(getCurrentSiteId());
    }
    
    public List<String> getIdMaintainerSiteMembers(String siteId){
		ArrayList<String> result = new ArrayList<String>();
    	try{
    		Site site = siteService.getSite(siteId);
    		Set<org.sakaiproject.authz.api.Member> members = site.getMembers();
    		for (org.sakaiproject.authz.api.Member sakaiMember: members){
    			if (isMaintainer(sakaiMember.getUserId(),siteId) && !sakaiMember.getUserEid().equals("admin"))
    				result.add(sakaiMember.getUserId());
    			
    		}
    		return result;
    	} catch (Exception e){
    		e.printStackTrace();
    		return result;
    	}   	
    }

	public boolean isCurrentUserMaintainer()
	{
		return isMaintainer(getCurrentUserId());
	}
	
	public boolean isCurrentUserAdmin()
	{
		String userId = getCurrentUserId();
		return userId.equals(userDirectoryService.ADMIN_ID);
	}

	public String getSakaiProperty(String string)
	{
		return serverConfigurationService.getString(string);
	}

	public BlogMember getMember(String memberId)
	{
		User user;
		try
		{
			user = userDirectoryService.getUser(memberId);
			BlogMember member = new BlogMember(user);
		
			return member;
		}
		catch (UserNotDefinedException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public void setToolManager(ToolManager toolManager)
	{
		this.toolManager = toolManager;
	}

	public ToolManager getToolManager()
	{
		return toolManager;
	}

	public void setSessionManager(SessionManager sessionManager)
	{
		this.sessionManager = sessionManager;
	}

	public SessionManager getSessionManager()
	{
		return sessionManager;
	}

	public void setAuthzGroupService(AuthzGroupService authzGroupService)
	{
		this.authzGroupService = authzGroupService;
	}

	public AuthzGroupService getAuthzGroupService()
	{
		return authzGroupService;
	}

	public String getCurrentSiteCollectionId()
	{
		return contentHostingService.getSiteCollection(toolManager.getCurrentPlacement().getContext());
	}
	
	public boolean isAutoDDL()
	{
		String autoDDL = serverConfigurationService.getString("auto.ddl");
		return autoDDL.equals("true");
	}

	/*
	public List<String> getEidMaintainerSiteMembers()
	{
		// TODO Auto-generated method stub
		return null;
	}
	*/

	public List<BlogMember> getSiteMembers()
	{
		ArrayList<BlogMember> result = new ArrayList<BlogMember>();
		try
		{
			Site site = siteService.getSite(getCurrentSiteId());
			Set<String> userIds = site.getUsers();
			for (String userId : userIds)
			{
				try
				{
					User sakaiUser = userDirectoryService.getUser(userId);
					BlogMember member = new BlogMember(sakaiUser);
					result.add(member);
				}
				catch(UserNotDefinedException unde)
				{
					logger.error("Failed to get site member details",unde);
				}
			}
			
			return result;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return result;
		}
	}
	public boolean fileExists(String resourceId) throws Exception
	{
		try
		{
			String fullId = "/group/" + this.getCurrentSiteId() + "/blog-resources/" + resourceId;
			ContentResource cr = contentHostingService.getResource(fullId);
			return cr != null;
		}
		catch(PermissionException e)
		{
			throw new Exception(e);
		}
		catch (IdUnusedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		catch (TypeException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Saves the file to Sakai's content hosting
	 */
	public String saveFile(Post post,String name,String mimeType, byte[] fileData)
	{
		if (logger.isDebugEnabled())
			logger.debug("saveFile(" + name + "," + mimeType + ",[BINARY FILE DATA])");

		try
		{
			String id = "/group/" + post.getSiteId() + "/blog-files/" + name;
			
			if(post.isPublic())
				id = "/public/blog-files/" + name;

			try
			{
				ContentResourceEdit resource = contentHostingService.addResource(id);
				resource.setContentType(mimeType);
				resource.setContent(fileData);
				ResourceProperties props = new BaseResourceProperties();
				props.addProperty(ResourceProperties.PROP_CONTENT_TYPE, mimeType);
				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
				props.addProperty(ResourceProperties.PROP_CREATOR, post.getCreatorId());
				resource.getPropertiesEdit().set(props);
				contentHostingService.commitResource(resource, NotificationService.NOTI_NONE);
				return resource.getId();
			}
			catch (IdUsedException idue)
			{
				if (logger.isInfoEnabled()) logger.info("ID '" + id + "' is in use.");
				
				return id;
			}
			catch (Exception e)
			{
				logger.error("Caught an execption whilst saving file. Returning null ...",e);
				return null;
			}
		}
		catch (Exception e)
		{
			if (logger.isDebugEnabled())
				e.printStackTrace();

			logger.error("Caught an execption with message'" + e.getMessage() + "'. Returning false ...");

			return null;
		}
	}
	
	public void getFile(File file)
	{
		try
		{
			ContentResource resource = contentHostingService.getResource(file.getResourceId());
			file.setContent(resource.getContent());
		}
		catch(Exception e)
		{
			if(logger.isDebugEnabled()) e.printStackTrace();
			
			logger.error("Caught an exception with message '" + e.getMessage()+ "'");
		}
		
	}
	
	public void getImage(Image image,int size)
	{
		if(logger.isDebugEnabled()) logger.debug("getImage(Image image," + size + ")");
		
		try
		{
			switch(size)
			{
				case Image.ORIGINAL:
				{
					ContentResource resource = contentHostingService.getResource(image.getFullResourceId());
					image.setFullContent(resource.getContent());
					break;
				}
				case Image.WEB:
				{
					ContentResource resource = contentHostingService.getResource(image.getWebResourceId());
					image.setImageContentWithWebSize(resource.getContent());
					break;
				}
				default:
					logger.warn("Invalid image size supplied: " + size);
			}
		}
		catch (Exception e)
		{
			if(logger.isDebugEnabled()) e.printStackTrace();
			
			logger.error("Caught an execption with message '" + e.getMessage()+ "'");
		}
	}
	
	public String getPortalUrl()
	{
		return serverConfigurationService.getPortalUrl();
	}
	
	public String getAccessUrl()
	{
		return serverConfigurationService.getAccessUrl();
	}

	public void setContentHostingService(ContentHostingService contentHostingService)
	{
		this.contentHostingService = contentHostingService;
	}

	public ContentHostingService getContentHostingService()
	{
		return contentHostingService;
	}

	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	public SiteService getSiteService()
	{
		return siteService;
	}

	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}

	public ServerConfigurationService getServerConfigurationService()
	{
		return serverConfigurationService;
	}

	public void setAuthenticationManager(AuthenticationManager authenticationManager)
	{
		this.authenticationManager = authenticationManager;
	}

	public AuthenticationManager getAuthenticationManager()
	{
		return authenticationManager;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}

	public UserDirectoryService getUserDirectoryService()
	{
		return userDirectoryService;
	}

	public void registerEntityProducer(EntityProducer entityProducer)
	{
		entityManager.registerEntityProducer(entityProducer, "blog");
	}

	public void setEntityManager(EntityManager entityManager)
	{
		this.entityManager = entityManager;
	}

	public EntityManager getEntityManager()
	{
		return entityManager;
	}

	public void deleteResources(String[] resourceIds)
	{
		for(String resourceId : resourceIds)
		{
			try
			{
				contentHostingService.removeResource(resourceId);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public Profile getUserProfileById(String userId)
	{
		return profileManager.getUserProfileById(userId);
	}

	public void setProfileManager(ProfileManager profileManager)
	{
		this.profileManager = profileManager;
	}

	public ProfileManager getProfileManager()
	{
		return profileManager;
	}
	
	public void deleteFolderForPost(String postId)
	{
		String collectionId = "/group/" + this.getCurrentSiteId() + "/blog-resources/" + postId;
		try
		{
			contentHostingService.removeResource(collectionId);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setSqlService(SqlService sqlService)
	{
		this.sqlService = sqlService;
	}

	public SqlService getSqlService()
	{
		return sqlService;
	}

	public String getResourceUrl(String resourceId)
	{
		return contentHostingService.getUrl(resourceId);
	}

	public boolean isOnGateway()
	{
        return "!gateway".equals(getCurrentSiteId());
	}

	public Profile getProfile(String userId)
	{
		return profileManager.getUserProfileById(userId);
	}

	public void registerFunction(String function)
	{
		List functions = functionManager.getRegisteredFunctions("blog.");
		
		if(!functions.contains(function))
			functionManager.registerFunction(function);
	}

	public void setFunctionManager(FunctionManager functionManager)
	{
		this.functionManager = functionManager;
	}

	public FunctionManager getFunctionManager()
	{
		return functionManager;
	}

	public boolean isAllowedFunction(String function)
	{
		try
		{
			Site site = siteService.getSite(getCurrentSiteId());
			AuthzGroup realm = authzGroupService.getAuthzGroup(site.getReference());
			Role r = realm.getUserRole(getCurrentUserId());
			return r.isAllowed(function);
		}
		catch (Exception e)
		{
			logger.error("Caught exception while performing function test",e);
		}
		
		return false;
	}

	public List<BlogPermissions> getPermissions()
	{
		try
		{
			Site site = siteService.getSite(getCurrentSiteId());
			AuthzGroup realm = authzGroupService.getAuthzGroup(site.getReference());
			Set<Role> roles = realm.getRoles();
			
			List<BlogPermissions> list = new ArrayList<BlogPermissions>(roles.size());
			
			for(Role role : roles)
			{
				BlogPermissions permissions = new BlogPermissions();
				permissions.setRole(role.getId());
				permissions.setPostCreate(role.isAllowed(BlogFunctions.BLOG_POST_CREATE));
				permissions.setPostReadAny(role.isAllowed(BlogFunctions.BLOG_POST_READ_ANY));
				permissions.setPostReadOwn(role.isAllowed(BlogFunctions.BLOG_POST_READ_OWN));
				permissions.setPostUpdateAny(role.isAllowed(BlogFunctions.BLOG_POST_UPDATE_ANY));
				permissions.setPostUpdateOwn(role.isAllowed(BlogFunctions.BLOG_POST_UPDATE_OWN));
				permissions.setPostDeleteAny(role.isAllowed(BlogFunctions.BLOG_POST_DELETE_ANY));
				permissions.setPostDeleteOwn(role.isAllowed(BlogFunctions.BLOG_POST_DELETE_OWN));
				permissions.setCommentCreate(role.isAllowed(BlogFunctions.BLOG_COMMENT_CREATE));
				permissions.setCommentReadAny(role.isAllowed(BlogFunctions.BLOG_COMMENT_READ_ANY));
				permissions.setCommentReadOwn(role.isAllowed(BlogFunctions.BLOG_COMMENT_READ_OWN));
				permissions.setCommentUpdateAny(role.isAllowed(BlogFunctions.BLOG_COMMENT_UPDATE_ANY));
				permissions.setCommentUpdateOwn(role.isAllowed(BlogFunctions.BLOG_COMMENT_UPDATE_OWN));
				permissions.setCommentDeleteAny(role.isAllowed(BlogFunctions.BLOG_COMMENT_DELETE_ANY));
				permissions.setCommentDeleteOwn(role.isAllowed(BlogFunctions.BLOG_COMMENT_DELETE_OWN));
				
				list.add(permissions);
			}
			
			return list;
		}
		catch(Exception e)
		{
			logger.error("Caught exception whilst building permissions list",e);
		}
		
		return null;
	}

	public void savePermissions(BlogPermissions permissions) throws Exception
	{
			Site site = siteService.getSite(getCurrentSiteId());
			AuthzGroup realm = authzGroupService.getAuthzGroup(site.getReference());
			Role role = realm.getRole(permissions.getRole());
			if(role == null)
				throw new Exception("Role '" + permissions.getRole() + "' has not been setup for this site");
			
			if(permissions.isPostCreate())
				role.allowFunction(BlogFunctions.BLOG_POST_CREATE);
			else
				role.disallowFunction(BlogFunctions.BLOG_POST_CREATE);
			
			if(permissions.isPostReadAny())
				role.allowFunction(BlogFunctions.BLOG_POST_READ_ANY);
			else
				role.disallowFunction(BlogFunctions.BLOG_POST_READ_ANY);
			
			if(permissions.isPostReadOwn())
				role.allowFunction(BlogFunctions.BLOG_POST_READ_OWN);
			else
				role.disallowFunction(BlogFunctions.BLOG_POST_READ_OWN);
			
			if(permissions.isPostUpdateAny())
				role.allowFunction(BlogFunctions.BLOG_POST_UPDATE_ANY);
			else
				role.disallowFunction(BlogFunctions.BLOG_POST_UPDATE_ANY);
			
			if(permissions.isPostUpdateOwn())
				role.allowFunction(BlogFunctions.BLOG_POST_UPDATE_OWN);
			else
				role.disallowFunction(BlogFunctions.BLOG_POST_UPDATE_OWN);
			
			if(permissions.isPostDeleteAny())
				role.allowFunction(BlogFunctions.BLOG_POST_DELETE_ANY);
			else
				role.disallowFunction(BlogFunctions.BLOG_POST_DELETE_ANY);
			
			if(permissions.isPostDeleteOwn())
				role.allowFunction(BlogFunctions.BLOG_POST_DELETE_OWN);
			else
				role.disallowFunction(BlogFunctions.BLOG_POST_DELETE_OWN);
			
			if(permissions.isCommentCreate())
				role.allowFunction(BlogFunctions.BLOG_COMMENT_CREATE);
			else
				role.disallowFunction(BlogFunctions.BLOG_COMMENT_CREATE);
			
			if(permissions.isCommentReadAny())
				role.allowFunction(BlogFunctions.BLOG_COMMENT_READ_ANY);
			else
				role.disallowFunction(BlogFunctions.BLOG_COMMENT_READ_ANY);
			
			if(permissions.isCommentReadOwn())
				role.allowFunction(BlogFunctions.BLOG_COMMENT_READ_OWN);
			else
				role.disallowFunction(BlogFunctions.BLOG_COMMENT_READ_OWN);
			
			if(permissions.isCommentUpdateAny())
				role.allowFunction(BlogFunctions.BLOG_COMMENT_UPDATE_ANY);
			else
				role.disallowFunction(BlogFunctions.BLOG_COMMENT_UPDATE_ANY);
			
			if(permissions.isCommentUpdateOwn())
				role.allowFunction(BlogFunctions.BLOG_COMMENT_UPDATE_OWN);
			else
				role.disallowFunction(BlogFunctions.BLOG_COMMENT_UPDATE_OWN);
			
			if(permissions.isCommentDeleteAny())
				role.allowFunction(BlogFunctions.BLOG_COMMENT_DELETE_ANY);
			else
				role.disallowFunction(BlogFunctions.BLOG_COMMENT_DELETE_ANY);
			
			if(permissions.isCommentDeleteOwn())
				role.allowFunction(BlogFunctions.BLOG_COMMENT_DELETE_OWN);
			else
				role.disallowFunction(BlogFunctions.BLOG_COMMENT_DELETE_OWN);
			
			authzGroupService.save(realm);
	}

	public void addEventObserver(Observer observer)
	{
		eventTrackingService.addObserver(observer);
	}

	public void setEventTrackingService(EventTrackingService eventTrackingService)
	{
		this.eventTrackingService = eventTrackingService;
	}

	public EventTrackingService getEventTrackingService()
	{
		return eventTrackingService;
	}

	public void deleteEventObserver(Observer observer)
	{
		eventTrackingService.deleteObserver(observer);
	}

	public String getIdForEid(String eid)
	{
		try
		{
			return userDirectoryService.getUserByEid(eid).getId();
		}
		catch (UserNotDefinedException e)
		{
			e.printStackTrace();
			return eid;
		}
	}

	public boolean siteExists(String siteId)
	{
		return siteService.siteExists(siteId);
	}

	public void allowFunction(String string, String blogPostCreate)
	{
		// TODO Auto-generated method stub
		
	}

	public boolean currentSiteHasRole(String string)
	{
		return false;
	}
}
