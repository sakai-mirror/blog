package org.sakaiproject.blog.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.blog.api.datamodel.BlogOptions;
import org.sakaiproject.blog.api.datamodel.BlogPermissions;
import org.sakaiproject.blog.api.datamodel.Modes;

public class OptionsPage extends BasePage
{
	private transient Logger logger = Logger.getLogger(OptionsPage.class);
	
	public OptionsPage()
	{
		super();
		
		final BlogOptions options = persistenceManager.getOptions();
		
		add(new Label("optionsLabel",new ResourceModel("options")));
		
		Form form = new Form("form");
		
		form.add(new Label("modeLabel",new ResourceModel("mode")));
		
		RadioGroup radioGroup = new RadioGroup("modes",new PropertyModel(options,"mode"));
		
		radioGroup.add(new Label("learningLogModeLabel",new ResourceModel("learningLogMode")));
		Radio learningLogRadio = new Radio("learningLog",new Model(Modes.LEARNING_LOG));
		radioGroup.add(learningLogRadio);
		
		radioGroup.add(new Label("blogModeLabel",new ResourceModel("blogMode")));
		Radio blogRadio = new Radio("blog",new Model(Modes.BLOG));
		radioGroup.add(blogRadio);
		
		form.add(radioGroup);
		
		form.add(new Button("saveButton",new ResourceModel("save"))
		{
			public void onSubmit()
			{
				if(options.isLearningLogMode())
				{
					try
					{
						BlogPermissions permissions = new BlogPermissions();
						permissions.setRole("Student");
				
						permissions.setPostCreate(true);
						permissions.setPostReadOwn(true);
						permissions.setPostDeleteOwn(true);
						permissions.setPostUpdateOwn(true);
						permissions.setCommentCreate(true);
						permissions.setCommentReadAny(true);
						permissions.setCommentReadOwn(true);
						permissions.setCommentUpdateOwn(true);
					
						sakaiProxy.savePermissions(permissions);
					}
					catch (Exception e)
					{
						logger.error("Caught exception whilst saving 'Student' permissions",e);
					}
					
					try
					{
						BlogPermissions permissions = new BlogPermissions();
						permissions.setRole("Tutor");
				
						permissions.setPostReadAny(true);
						permissions.setPostCreate(false);
						permissions.setPostReadOwn(false);
						permissions.setPostUpdateOwn(false);
						permissions.setPostDeleteOwn(false);
						permissions.setCommentCreate(true);
						permissions.setCommentReadAny(true);
						permissions.setCommentReadOwn(true);
						permissions.setCommentUpdateOwn(true);
						permissions.setCommentDeleteOwn(true);
					
						sakaiProxy.savePermissions(permissions);
					}
					catch (Exception e)
					{
						logger.error("Caught exception whilst saving 'Tutor' permissions",e);
					}
				}
					
				try
				{
					persistenceManager.saveOptions(options);
				}
				catch (Exception e)
				{
					logger.error("Caught exception whilst saving options",e);
				}
				
				setResponsePage(new ViewAll());
			}
		});
		
		Button cancelButton = new Button("cancelButton",new ResourceModel("cancel"))
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
