package org.sakaiproject.tool.blog.dataproviders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.sakaiproject.tool.blog.BlogApplication;
import org.sakaiproject.tool.blog.api.BlogMember;
import org.sakaiproject.tool.blog.impl.managers.PersistenceManager;
import org.sakaiproject.tool.blog.pages.models.MemberModel;

/**
 * This class provides the user details of Sakai user's who have marked blog
 * entries as PUBLIC.
 * 
 * @author Adrian Fish (a.fish@lancaster.ac.uk)
 *
 */
public class PublicBloggerDataProvider implements IDataProvider
{
	private List<BlogMember> members = new ArrayList<BlogMember>();
	
	public PublicBloggerDataProvider()
	{
		PersistenceManager persistenceManager = BlogApplication.get().getPersistenceManager();
		members = persistenceManager.getPublicBloggers();
	}

	public Iterator iterator(int first, int count)
	{
		try
		{
			List<BlogMember> slice = members.subList(first, first + count);
			return slice.iterator();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Collections.EMPTY_LIST.iterator();
		}
	}

	public IModel model(Object object)
	{
		return new MemberModel((BlogMember) object);
	}

	public int size()
	{
		if (members == null)
			return 0;

		return members.size();
	}

	public void detach()
	{
	}
}
