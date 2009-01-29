package org.sakaiproject.blog.tool.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.StringValidator;
import org.sakaiproject.blog.tool.BlogApplication;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.datamodel.State;
import org.sakaiproject.blog.api.BlogManager;
import org.sakaiproject.blog.tool.pages.models.PostModel;
import org.sakaiproject.blog.tool.pages.validators.TitleValidator;
import org.sakaiproject.wicket.markup.html.fckeditor.FCKEditorPanel;

public class EditPostForm extends Form
{
	private transient Logger logger = Logger.getLogger(EditPostForm.class);

	private PostModel postModel;

	private Post post;

	private transient BlogManager blogManager;
	
	public EditPostForm(String id,final PostModel postModel)
	{
		super(id);
		
		blogManager = BlogApplication.get().getBlogManager();
		
		this.postModel = postModel;
		
		post = (Post) this.postModel.getObject();
		
		add(new Label("titleLabel",new ResourceModel("postTitle")));
		String cId = BlogApplication.get().getSakaiProxy().getCurrentSiteCollectionId();

		TextField titleField = new TextField("titleField",new PropertyModel(post,"title"));
		titleField.add(new TitleValidator());
		titleField.setRequired(false);
		add(titleField);
		
		add(new Label("keywordsLabel",new ResourceModel("keywords")));
		TextField keywordsField = new TextField("keywordsField",new PropertyModel(post,"keywords"));
		add(keywordsField);
		
		add(new Label("postVisibilityLabel",new ResourceModel("postVisibility")));
		
		List<String> temp = Arrays.asList(new String[] {State.PRIVATE,State.READY});
		List<String> model = new ArrayList<String>(temp);
		if(!BlogApplication.get().getPersistenceManager().getOptions().isLearningLogMode())
			model.add(State.PUBLIC);
		
		add(new DropDownChoice("visibilities", new PropertyModel(post,"visibility"), model));
		
		CheckBox readOnlyCheckbox = new CheckBox("readOnlyCheckbox", new PropertyModel(post,"readOnly"));
		add(readOnlyCheckbox);
		Label readOnlyLabel = new Label("readOnlyLabel",new ResourceModel("readOnly"));
		add(readOnlyLabel);
		
		CheckBox allowCommentsCheckbox = new CheckBox("allowCommentsCheckbox", new PropertyModel(post,"commentable"));
		add(allowCommentsCheckbox);
		Label allowCommentsLabel = new Label("allowCommentsLabel",new ResourceModel("allowComments"));
		add(allowCommentsLabel);
		
		if(BlogApplication.get().getPersistenceManager().getOptions().isLearningLogMode())
		{
			readOnlyCheckbox.setVisible(false);
			readOnlyLabel.setVisible(false);
			allowCommentsCheckbox.setVisible(false);
			allowCommentsLabel.setVisible(false);
		}
		
		add(new Label("abstractLabel",new ResourceModel("abstract")));
		add(new FCKEditorPanel("abstractEditor",new PropertyModel(post,"shortText"),"650","100","Basic",cId,true));
		
		Button saveButton = new Button("saveButton",new ResourceModel("save"));
		
		/*
		Button saveButton = new Button("saveButton",new ResourceModel("save"))
		{
			public void onSubmit()
			{
				try
				{
					blogManager.createPost(post);
					setResponsePage(new PostPage(postModel,true));
				}
				catch (Exception e)
				{
					logger.error("Caught exception whilst saving post.",e);
				}
			}
		};
		*/
		
		add(saveButton);
		
		Button cancelButton = new Button("cancelButton",new ResourceModel("cancel"))
		{
			public void onSubmit()
			{
				if(BlogApplication.get().getPersistenceManager().getOptions().isLearningLogMode())
					setResponsePage(new MemberBlog());
				else
					setResponsePage(new ViewAll());
			}
		};
		
		cancelButton.setDefaultFormProcessing(false);
		
		add(cancelButton);
	}
	
	public void onSubmit()
	{
		try
		{
			if(!hasError())
			{
				blogManager.createPost(post);
				setResponsePage(new PostPage(postModel,true));
			}
		}
		catch (Exception e)
		{
			logger.error("Caught exception whilst saving post.",e);
		}
	}
}
