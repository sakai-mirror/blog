package org.sakaiproject.tool.blog.dataproviders;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.tool.blog.BlogApplication;
import org.sakaiproject.tool.blog.api.SakaiProxy;
import org.sakaiproject.tool.blog.api.datamodel.BlogPermissions;
import org.sakaiproject.tool.blog.api.datamodel.Post;

public class PermissionsDataProvider implements IDataProvider
{
	private transient SakaiProxy sakaiProxy;

	public PermissionsDataProvider()
	{
		sakaiProxy = BlogApplication.get().getSakaiProxy();
	}
	
	public List<BlogPermissions> getData()
	{
		return sakaiProxy.getPermissions();
	}

	public Iterator iterator(int first, int count)
	{
		List<BlogPermissions> list = sakaiProxy.getPermissions();
		return list.subList(first,first + count).iterator();
	}

	public IModel model(Object object)
	{
		IModel model = new Model();
		model.setObject(object);
		return model;
	}

	public int size()
	{
		return sakaiProxy.getPermissions().size();
	}

	public void detach()
	{
		// TODO Auto-generated method stub
	}
}
