package org.sakaiproject.blog.tool.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.blog.api.datamodel.Preferences;

public class PreferencesPage extends BasePage
{
	public PreferencesPage()
	{
		super();
		
		preferencesLink.setVisible(false);
		
		add(new Label("preferencesLabel",new ResourceModel("preferences")));
		
		final Preferences preferences = persistenceManager.getPreferences();
		
		Form form = new Form("form");
		
		form.add(new CheckBox("newPostAlertCheckbox",new PropertyModel(preferences,"newPostAlert")));
		form.add(new Label("newPostAlertLabel",new ResourceModel("newPostAlert")));
		form.add(new CheckBox("newCommentAlertCheckbox",new PropertyModel(preferences,"newCommentAlert")));
		form.add(new Label("newCommentAlertLabel",new ResourceModel("newCommentAlert")));
		
		form.add(new Button("saveButton",new ResourceModel("save"))
		{
			public void onSubmit()
			{
				try
				{
					persistenceManager.savePreferences(preferences);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
				setResponsePage(new ViewAll());
			}
		});
		
		Button cancelButton = new Button("cancelButton", new ResourceModel("cancel"))
		{
			public void onSubmit()
			{
				setResponsePage(new ViewAll());
			}
		};
		
		cancelButton.setDefaultFormProcessing(false);

		form.add(cancelButton);
		
		add(form);
	}
}
