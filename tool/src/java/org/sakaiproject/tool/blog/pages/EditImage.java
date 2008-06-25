package org.sakaiproject.tool.blog.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.tool.blog.api.datamodel.Image;
import org.sakaiproject.tool.blog.api.datamodel.Post;
import org.sakaiproject.tool.blog.api.util.JpegTransformer;
import org.sakaiproject.tool.blog.imagecache.CacheForImages;
import org.sakaiproject.tool.blog.pages.models.PostModel;

public class EditImage extends BasePage
{
	private Post post;

	private int elementIndex;

	private FileUploadField imageField;

	private Button saveButton;

	private Image image = new Image();

	protected String imageDescription;

	public EditImage(Post post, int elementIndex, final boolean modify)
	{
		this.post = post;
		this.elementIndex = elementIndex;

		if (modify)
			this.image = (Image) post.getElement(elementIndex);
		
		add(new Label("editImage",new ResourceModel("editImage")));
		
		Form form = new Form("form");

		form.add(new Label("imageLabel", "Image:")); // TODO: Moke a resource
		imageField = new FileUploadField("imageField");
		form.add(imageField);

		saveButton = new Button("saveButton", new ResourceModel("save"))
		{
			public void onSubmit()
			{
				FileUpload fileUpload = imageField.getFileUpload();
				if (fileUpload != null)
				{
					byte[] content = fileUpload.getBytes();
					try
					{
						if (content != null)
						{
							imageDescription = fileUpload.getClientFileName();

							if (imageDescription.indexOf(":\\") == 1) // we assume that is a windows file comming from ie
								imageDescription = imageDescription.substring(imageDescription.lastIndexOf("\\") + 1);

							//Image image = new Image();
							image.setFileName(imageDescription);
							image.setFullContent(content);
							
							if(modify)
							{
								EditImage.this.postManager.replaceElement(EditImage.this.post,image, EditImage.this.elementIndex);
							}
							else
								EditImage.this.postManager.addElement(EditImage.this.post,image,EditImage.this.elementIndex);
							
							setResponsePage(new PostPage(new PostModel(EditImage.this.post),true));
						}
					}
					catch (Exception e)
					{
						// if we can not treat the Image because of the format, it will be treat as a file
						image = null;
						imageDescription = "";
					}
				}
			}
		};

		form.add(saveButton);

		Button cancelButton = new Button("cancelButton", new ResourceModel("cancel"))
		{
			public void onSubmit()
			{
				setResponsePage(new PostPage(new PostModel(EditImage.this.post),true));
			}
		};
		
		cancelButton.setDefaultFormProcessing(false);
		
		form.add(cancelButton);
		
		add(form);
	}
}