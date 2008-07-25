package org.sakaiproject.blog.tool.pages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.blog.api.datamodel.BlogPermissions;
import org.sakaiproject.blog.tool.dataproviders.PermissionsDataProvider;

public class PermissionsPage extends BasePage
{
	private Logger logger = Logger.getLogger(PermissionsPage.class);
	
	private Map<String,BlogPermissions> map;

	public PermissionsPage()
	{
		super();
		
		final PermissionsDataProvider provider = new PermissionsDataProvider();
		
		List<BlogPermissions> data = provider.getData();
		
		map = new HashMap<String,BlogPermissions>(data.size());
		
		for(BlogPermissions permissions : data)
			map.put(permissions.getRole(),permissions);
		
		add(new Label("permissionsLabel",new ResourceModel("permissions")));
		
		Form form = new Form("form");
		
		form.add(new Label("roleLabel",new ResourceModel("role")));
		form.add(new Label("postCreateLabel",new ResourceModel("create")));
		form.add(new Label("postReadAnyLabel",new ResourceModel("readAny")));
		form.add(new Label("postReadOwnLabel",new ResourceModel("readOwn")));
		form.add(new Label("postUpdateAnyLabel",new ResourceModel("updateAny")));
		form.add(new Label("postUpdateOwnLabel",new ResourceModel("updateOwn")));
		form.add(new Label("postDeleteAnyLabel",new ResourceModel("deleteAny")));
		form.add(new Label("postDeleteOwnLabel",new ResourceModel("deleteOwn")));
		form.add(new Label("commentCreateLabel",new ResourceModel("create")));
		form.add(new Label("commentReadAnyLabel",new ResourceModel("readAny")));
		form.add(new Label("commentReadOwnLabel",new ResourceModel("readOwn")));
		form.add(new Label("commentUpdateAnyLabel",new ResourceModel("updateAny")));
		form.add(new Label("commentUpdateOwnLabel",new ResourceModel("updateOwn")));
		form.add(new Label("commentDeleteAnyLabel",new ResourceModel("deleteAny")));
		form.add(new Label("commentDeleteOwnLabel",new ResourceModel("deleteOwn")));
		
		DataView permissionsTable = new DataView("permissionsTable",provider)
		{
			@Override
			protected void populateItem(Item item)
			{
				BlogPermissions permissions = (BlogPermissions) item.getModelObject();
				
				String role = permissions.getRole();
				
				item.add(new Label("role",role));
				
				item.add(new CheckBox("postCreateCheckbox",new PropertyModel(map.get(role),"postCreate")));
				item.add(new CheckBox("postReadAnyCheckbox",new PropertyModel(map.get(role),"postReadAny")));
				item.add(new CheckBox("postReadOwnCheckbox",new PropertyModel(map.get(role),"postReadOwn")));
				item.add(new CheckBox("postUpdateAnyCheckbox",new PropertyModel(map.get(role),"postUpdateAny")));
				item.add(new CheckBox("postUpdateOwnCheckbox",new PropertyModel(map.get(role),"postUpdateOwn")));
				item.add(new CheckBox("postDeleteAnyCheckbox",new PropertyModel(map.get(role),"postDeleteAny")));
				item.add(new CheckBox("postDeleteOwnCheckbox",new PropertyModel(map.get(role),"postDeleteOwn")));
				item.add(new CheckBox("commentCreateCheckbox",new PropertyModel(map.get(role),"commentCreate")));
				item.add(new CheckBox("commentReadAnyCheckbox",new PropertyModel(map.get(role),"commentReadAny")));
				item.add(new CheckBox("commentReadOwnCheckbox",new PropertyModel(map.get(role),"commentReadOwn")));
				item.add(new CheckBox("commentUpdateAnyCheckbox",new PropertyModel(map.get(role),"commentUpdateAny")));
				item.add(new CheckBox("commentUpdateOwnCheckbox",new PropertyModel(map.get(role),"commentUpdateOwn")));
				item.add(new CheckBox("commentDeleteAnyCheckbox",new PropertyModel(map.get(role),"commentDeleteAny")));
				item.add(new CheckBox("commentDeleteOwnCheckbox",new PropertyModel(map.get(role),"commentDeleteOwn")));
			}
		};
		
		form.add(permissionsTable);
		
		form.add(new Button("saveButton",new ResourceModel("save"))
		{
			public void onSubmit()
			{
				for(String role : map.keySet())
				{
					BlogPermissions permissions = map.get(role);
					try
					{
						sakaiProxy.savePermissions(permissions);
					}
					catch (Exception e)
					{
						logger.error("Caught exception whilst saving permissions.",e);
					}
				}
				
				setResponsePage(new ViewAll());
			}
		});
		
		form.add(new Button("cancelButton",new ResourceModel("cancel"))
		{
			public void onSubmit()
			{
				setResponsePage(new ViewAll());
			}
		});
		
		add(form);
	}
}
