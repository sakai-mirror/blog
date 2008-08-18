package org.sakaiproject.blog.tool.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.blog.tool.BlogApplication;
import org.sakaiproject.blog.api.datamodel.Paragraph;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.tool.pages.models.PostModel;
import org.sakaiproject.wicket.markup.html.fckeditor.FCKEditorPanel;

public class EditText extends BasePage
{
	private Post post;

	private int elementIndex;

	private Paragraph text = new Paragraph();

	private Button saveButton;

	public EditText(Post post, int elementIndex,final boolean modify)
	{
		super();
		this.post = post;
		this.elementIndex = elementIndex;
		
		if(modify)
			this.text = (Paragraph) post.getElement(elementIndex);
		// TODO Auto-generated constructor stub
		
		add(new Label("editText",new ResourceModel("editText")));

		Form form = new Form("form");

		String cId = BlogApplication.get().getSakaiProxy().getCurrentSiteCollectionId();

		form.add(new FCKEditorPanel("paragraphEditor", new PropertyModel(text,"text"), "650", "300", "Default", cId,true));
		saveButton = new Button("saveButton", new ResourceModel("save"))
		{
			public void onSubmit()
			{
				if(modify)
					//EditText.this.post.replaceElement(new Paragraph(text.getText()), EditText.this.elementIndex);
					EditText.this.blogManager.replaceElement(EditText.this.post, text, EditText.this.elementIndex);
				else
					EditText.this.blogManager.addElement(EditText.this.post,text,EditText.this.elementIndex);

				setResponsePage(new PostPage(new PostModel(EditText.this.post),true));
			}
		};

		form.add(saveButton);
		
		Button cancelButton = new Button("cancelButton", new ResourceModel("cancel"))
		{
			public void onSubmit()
			{
				setResponsePage(new PostPage(new PostModel(EditText.this.post),true));
			}
		};
		
		cancelButton.setDefaultFormProcessing(false);

		form.add(cancelButton);

		add(form);
	}
}