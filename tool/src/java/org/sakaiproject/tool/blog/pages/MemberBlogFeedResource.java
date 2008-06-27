package org.sakaiproject.tool.blog.pages;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sakaiproject.tool.blog.BlogApplication;
import org.sakaiproject.tool.blog.api.QueryBean;
import org.sakaiproject.tool.blog.api.SakaiProxy;
import org.sakaiproject.tool.blog.api.datamodel.Post;
import org.sakaiproject.tool.blog.impl.managers.PostManager;
//import org.wicketstuff.rome.FeedResource;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

public class MemberBlogFeedResource// extends FeedResource
{
	private transient PostManager postManager;
	private transient SakaiProxy sakaiProxy;
	private String userId;
	
	public MemberBlogFeedResource(String userId)
	{
		super();
		
		this.userId = userId;
		
		postManager = BlogApplication.get().getPostManager();
		sakaiProxy = BlogApplication.get().getSakaiProxy();
	}
	
	protected SyndFeed getFeed()
	{
		String displayName = sakaiProxy.getDisplayNameForTheUser(userId);
	    SyndFeed feed = new SyndFeedImpl();
	    feed.setFeedType("rss_2.0");
	    feed.setTitle(displayName + "'s Blog");
	    feed.setLink("http://mysite.com"); 
	    feed.setDescription("The Blog of " + displayName);
	    feed.setPublishedDate(new Date());
	    
	    QueryBean qb = new QueryBean();
	    qb.setCreator(userId);
	    qb.setSiteId(sakaiProxy.getCurrentSiteId());
	    List<SyndEntry> entries = new ArrayList<SyndEntry>();
	    try
	    {
	    	List<Post> posts = postManager.getPosts(qb);
	    	
	    	for(Post post : posts)
	    	{
	    SyndEntry entry = new SyndEntryImpl();
	    entry.setTitle(post.getTitle());
	    entry.setLink(post.getUrl());
	    entry.setPublishedDate(post.getCreatedDate());

	    SyndContent description = new SyndContentImpl();
	    description.setType("text/plain");
	    description.setValue(post.getShortText());
	    
	    entry.setDescription(description);

	    entries.add(entry);
	    	}
	    }
	    catch(Exception e)
	    {
	    	
	    }

	    feed.setEntries(entries);
	    return feed;  
	  }
}
