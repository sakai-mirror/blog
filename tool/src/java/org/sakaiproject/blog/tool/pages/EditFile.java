package org.sakaiproject.blog.tool.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.blog.api.datamodel.File;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.tool.pages.models.PostModel;

public class EditFile extends BasePage
{
	private File file = new File();

	private Post post;
	
	private FileUploadField fileField;
	
	private Button saveButton;

	private int elementIndex;

	public EditFile(Post post, int elementIndex,final boolean modify)
	{
		super();
		
		this.post = post;
		this.elementIndex = elementIndex;
		
		if(modify)
			this.file = (File) post.getElement(elementIndex);
		
		add(new Label("editFile",new ResourceModel("editFile")));
		
		Form form = new Form("form");
		
		form.add(new Label("nameLabel","Name:"));
		form.add(new TextField("nameField",new PropertyModel(file,"displayName")));

		fileField = new FileUploadField("fileField");
		form.add(fileField);

		saveButton = new Button("saveButton", new ResourceModel("save"))
		{
			public void onSubmit()
			{
				if (fileField == null) return;

				FileUpload fileUpload = fileField.getFileUpload();
				
				setFileUpload(fileUpload);
				
				if(!modify)
					EditFile.this.blogManager.addElement(EditFile.this.post,file,EditFile.this.elementIndex);
				else
	                EditFile.this.blogManager.replaceElement(EditFile.this.post,EditFile.this.file, EditFile.this.elementIndex);
				
				setResponsePage(new PostPage(new PostModel(EditFile.this.post),true));
			}
		};

		form.add(saveButton);

		Button cancelButton = new Button("cancelButton", new ResourceModel("cancel"))
		{
			public void onSubmit()
			{
				setResponsePage(new PostPage(new PostModel(EditFile.this.post),true));
			}
		};
		
		cancelButton.setDefaultFormProcessing(false);

		form.add(cancelButton);
		
		add(form);
	}

	private void setFileUpload(FileUpload fileUpload)
	{
		if (fileUpload != null)
		{
			try
			{
				byte[] content = fileUpload.getBytes();
				String mimeType = fileUpload.getContentType();
				String fileName = fileUpload.getClientFileName();
				if (fileName.indexOf(":\\") == 1) // we assume that is a windows file comming from ie
					fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
				
				file.setFileName(fileName);
				file.setContent(content);
				file.setMimeType(mimeType);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}