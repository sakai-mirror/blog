package org.sakaiproject.blogadministrator.tool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.sakaiproject.blog.api.BlogManager;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blogadministrator.tool.api.xml.XMLToPost;

public class Migrator
{
	private Logger logger = Logger.getLogger(Migrator.class);
	
	public static final String BLOG = "BLOG";
	public static final String LEARNINGLOG = "LEARNINGLOG";

	public String migrate()
	{
		if(logger.isDebugEnabled()) logger.debug("migrate()");
		
		StringBuilder report = new StringBuilder();
		
		int count = 0;
		
		SakaiProxy sakaiProxy = BlogDataMigratorApplication.get().getSakaiProxy();
		
		BlogManager blogManager = BlogDataMigratorApplication.get().getBlogManager();
		
		try
		{
			Connection connection = sakaiProxy.borrowConnection();
			
			Statement oldStatement = connection.createStatement();
			
			report.append("Selecting posts from BLOGGER_POST ...\n");
			
			ResultSet sourcePosts = oldStatement.executeQuery("SELECT * FROM BLOGGER_POST");
			XMLToPost xmlToPost = new XMLToPost();
			
			while(sourcePosts.next())
			{
				String sourcePostId = sourcePosts.getString("POST_ID");
				String siteId = sourcePosts.getString("SITE_ID");
				
				report.append("Source post with id '" + sourcePostId + "' is from the site with id '" + siteId + "'\n");
				
				// Does this site exist?
				if(!sakaiProxy.siteExists(siteId))
				{
					report.append("Old site with id '" + siteId + "' does not exist in this Sakai. Skipping post with id '" + sourcePostId + "' ...\n");
					continue;
				}
				
				String xml = sourcePosts.getString("XML");
				
				report.append("Building new post from source post's XML ...\n");
				Post post  = xmlToPost.convertXMLInPost(BLOG,xml,siteId);
				
				report.append("Saving post with id '" + post.getId() + "' in site '" + siteId + "' ...\n");
				blogManager.savePost(post);
				report.append("Saved post with id '" + post.getId() + "'\n");
				count++;
			}
			
			report.append("Selecting posts from LEARNINGLOG_POST ...\n");
			
			sourcePosts = oldStatement.executeQuery("SELECT * FROM LEARNINGLOG_POST");
			
			while(sourcePosts.next())
			{
				String sourcePostId = sourcePosts.getString("POST_ID");
				String siteId = sourcePosts.getString("SITE_ID");
				
				report.append("Source post with id '" + sourcePostId + "' is from the site with id '" + siteId + "'\n");
				
				// Does this site exist?
				if(!sakaiProxy.siteExists(siteId))
				{
					report.append("Old site with id '" + siteId + "' does not exist in this Sakai. Skipping post with id '" + sourcePostId + "' ...\n");
					continue;
				}
				
				String xml = sourcePosts.getString("XML");
				
				report.append("Building new post from source post's XML ...\n");
				Post post = xmlToPost.convertXMLInPost(LEARNINGLOG,xml,siteId);
				
				report.append("Saving post with id '" + post.getId() + "' in site '" + siteId + "' ...\n");
				blogManager.savePost(post);
				report.append("Saved post with id '" + post.getId() + "'\n");
				count++;
			}
		}
		catch(Exception sqle)
		{
			sqle.printStackTrace();
		}
		
		report.append("Successfully migrated " + count + " posts.\n");
		
		return report.toString();
	}
}
