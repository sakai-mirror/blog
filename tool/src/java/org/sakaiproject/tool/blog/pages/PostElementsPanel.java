package org.sakaiproject.tool.blog.pages;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.sakaiproject.tool.blog.BlogApplication;
import org.sakaiproject.tool.blog.api.SakaiProxy;
import org.sakaiproject.tool.blog.api.datamodel.File;
import org.sakaiproject.tool.blog.api.datamodel.Image;
import org.sakaiproject.tool.blog.api.datamodel.LinkRule;
import org.sakaiproject.tool.blog.api.datamodel.Paragraph;
import org.sakaiproject.tool.blog.api.datamodel.Post;
import org.sakaiproject.tool.blog.api.datamodel.PostElement;
import org.sakaiproject.tool.blog.dataproviders.PostElementsDataProvider;
import org.sakaiproject.tool.blog.impl.managers.PersistenceException;
import org.sakaiproject.tool.blog.impl.managers.PostManager;

public class PostElementsPanel extends Panel
{
	private transient PostManager postManager;

	private transient SakaiProxy sakaiProxy;

	private boolean editMode;

	private Post post;

	public PostElementsPanel(String id, final Post post)
	{
		super(id);

		this.post = post;

		postManager = BlogApplication.get().getPostManager();
		sakaiProxy = BlogApplication.get().getSakaiProxy();

		PostElementsDataProvider elementsProvider = new PostElementsDataProvider(post);

		DataView postElements = new DataView("postElements", elementsProvider)
		{
			@Override
			protected void populateItem(Item elementItem)
			{
				final PostElement postElement = (PostElement) elementItem.getModelObject();

				Link moveRightLink = new Link("moveRightLink")
				{
					@Override
					public void onClick()
					{
						postManager.setIndentation(post,postElement,postElement.getIndentation() + 1);
					}
				};

				elementItem.add(moveRightLink);

				moveRightLink.setVisible(editMode);

				Link moveLeftLink = new Link("moveLeftLink")
				{
					@Override
					public void onClick()
					{
						postManager.setIndentation(post,postElement,postElement.getIndentation() - 1);
					}
				};

				elementItem.add(moveLeftLink);

				moveLeftLink.setVisible(editMode);

				Link moveUpLink = new Link("moveUpLink")
				{
					@Override
					public void onClick()
					{
						postManager.moveUp(post,postElement);
					}
				};

				elementItem.add(moveUpLink);

				moveUpLink.setVisible(false);
				
				Link newLink = new Link("newLink")
				{
					@Override
					public void onClick()
					{
						int index = ((Item) getParent()).getIndex();
						setResponsePage(new ElementMenu(post,index));
					}
				};
				
				newLink.setVisible(editMode);

				elementItem.add(newLink);

				Link moveDownLink = new Link("moveDownLink")
				{
					@Override
					public void onClick()
					{
						postManager.moveDown(post,postElement);
					}
				};

				elementItem.add(moveDownLink);
				moveDownLink.setVisible(false);

				if (editMode)
				{
					int size = getDataProvider().size();
					
					if (size == 1)
					{
						moveUpLink.setVisible(false);
						moveDownLink.setVisible(false);
					}
					else if (elementItem.getIndex() == 0)
					{
						moveUpLink.setVisible(false);
						moveDownLink.setVisible(true);
					}
					else if (elementItem.getIndex() == (this.getDataProvider().size() - 1))
					{
						moveUpLink.setVisible(true);
						moveDownLink.setVisible(false);
					}
					else
					{
						moveUpLink.setVisible(true);
						moveDownLink.setVisible(true);
					}
				}
				
				Link deleteLink = new Link("deleteLink")
				{
					@Override
					public void onClick()
					{
						Item item = (Item) getParent();
						
						int index = item.getIndex();
						System.out.println("deleteButton clicked on index " + index);
						
						PostElement element = post.getElement(index);
						if (element instanceof Image)
						{
							//CacheForImages cache = CacheForImages.getInstance();
							//cache.removeImage(((Image) element).getId());
						}
						
						postManager.deleteElement(post,index);
						
						//post.removeElement(index);
						
						//postManager.savePost(post);
					}
				};

				elementItem.add(deleteLink);
				deleteLink.setVisible(editMode);
				
				Link editLink = new Link("editLink")
				{
					@Override
					public void onClick()
					{
						Item item = (Item) getParent();
						
						int index = item.getIndex();
						System.out.println("editButton clicked on index " + index);
						
						PostElement element = post.getElement(index);
						
						if (element instanceof Image)
						{
							setResponsePage(new EditImage(post,index,true));
						}
						
						else if(element instanceof LinkRule)
						{
							setResponsePage(new EditLink(post,index,true));
						}
						
						else if (element instanceof Paragraph)
						{
							setResponsePage(new EditText(post,index,true));
						}
						
						else if (element instanceof File)
						{
							setResponsePage(new EditFile(post,index,true));
						}
					}
				};

				elementItem.add(editLink);
				editLink.setVisible(editMode);

				int indentation = postElement.getIndentation();
				int pixels = indentation * 20;

				if (postElement instanceof Paragraph)
				{
					String text = ((Paragraph) postElement).getText();
					Label elementText = new Label("elementPanel", text);
					elementText.add(new AttributeAppender("style", true,new Model("padding-left: " + pixels + "px"),""));
					elementText.setEscapeModelStrings(false);
					elementItem.add(elementText);
				}
				// TODO Auto-generated method stub
				if (postElement instanceof Image)
				{
					try
					{
						String imageId = ((Image) postElement).getId();
						final Image image = postManager.getImage(imageId, Image.WEB);
					
						BufferedDynamicImageResource webResource = new BufferedDynamicImageResource()
						{
							protected byte[] getImageData()
							{
								return image.getImageContentWithWebSize();
							}
						};
					
						String url = sakaiProxy.getResourceUrl(image.getFullResourceId());
					
						org.apache.wicket.markup.html.image.Image wicketImage = new org.apache.wicket.markup.html.image.Image("elementPanel", webResource)
						{
							protected void onComponentTag(ComponentTag tag)
							{
								tag.setName("img");
								tag.setHasNoCloseTag(true);
								super.onComponentTag(tag);
							}

							protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
							{
								replaceComponentTagBody(markupStream, openTag, "");
								super.onComponentTagBody(markupStream, openTag);
							}
						};

						wicketImage.add(new SimpleAttributeModifier("style", "padding-left: " + pixels + "px"));
						wicketImage.add(new SimpleAttributeModifier("onClick", "launchFullSizeImage('" + image.getFileName() + "','" + url + "');"));

						elementItem.add(wicketImage);
					}
					catch (PersistenceException e)
					{
						e.printStackTrace();
					}
				}

				if (postElement instanceof File)
				{
					try
					{
						final File file = postManager.getFile(((File) postElement).getId());
						
						String mimeType = file.getMimeType();
						
						String iconUrl = "/library/image/silk/server.png";
						
						if(mimeType.equals("application/msword"))
							iconUrl = "/library/image/silk/page_word.png";
						else if(mimeType.equals("application/vnd.ms-word"))
							iconUrl = "/library/image/silk/page_word.png";
						else if(mimeType.equals("application/pdf"))
							iconUrl = "/library/image/silk/page_white_acrobat.png";
						else if(mimeType.equals("application/msexcel"))
							iconUrl = "/library/image/silk/page_excel.png";
						else if(mimeType.equals("application/vnd.ms-excel"))
							iconUrl = "/library/image/silk/page_excel.png";
						else if(mimeType.equals("application/mspowerpoint"))
							iconUrl = "/library/image/silk/page_white_powerpoint.png";
						else if(mimeType.equals("application/vnd.ms-powerpoint"))
							iconUrl = "/library/image/silk/page_white_powerpoint.png";
						else if(mimeType.equals("text/plain"))
							iconUrl = "/library/image/silk/text_dropcaps.png";
						else if(mimeType.equals("text/richtext"))
							iconUrl = "/library/image/silk/page_word.png";
						else if(mimeType.equals("audio/mpeg"))
							iconUrl = "/library/image/silk/sound.png";
						else
						{
							// GUESS !!!
							if(file.getFileName().endsWith(".doc"))
								iconUrl = "/library/image/silk/page_word.png";
							else if(file.getFileName().endsWith(".rtf"))
								iconUrl = "/library/image/silk/page_word.png";
							else if(file.getFileName().endsWith(".xls"))
								iconUrl = "/library/image/silk/page_excel.png";
							else if(file.getFileName().endsWith(".pdf"))
								iconUrl = "/library/image/silk/page_white_acrobat.png";
							else if(file.getFileName().endsWith(".ppt"))
								iconUrl = "/library/image/silk/page_white_powerpoint.png";
							else if(file.getFileName().endsWith(".txt"))
								iconUrl = "/library/image/silk/text_dropcaps.png";
							else if(file.getFileName().endsWith(".mp3"))
								iconUrl = "/library/image/silk/sound.png";
							else
								iconUrl = "/library/image/silk/server.png";
						}

						WebResource resource = new WebResource()
						{
							@Override
							public IResourceStream getResourceStream()
							{
								return new AbstractResourceStream()
								{

									public void close() throws IOException
									{
									}

									public InputStream getInputStream() throws ResourceStreamNotFoundException
									{
										sakaiProxy.getFile(file);
										return new ByteArrayInputStream(file.getContent());
									}
								};
							}

							@Override
							public void setHeaders(WebResponse response)
							{
								response.setContentType(file.getMimeType());
								response.setAttachmentHeader(file.getFileName());
							}
						};

						ResourceLink fileLink = new ResourceLink("link", resource)
						{
							protected void onComponentTag(ComponentTag tag)
							{
								tag.setName("a");
								super.onComponentTag(tag);
							}

							protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
							{
								replaceComponentTagBody(markupStream, openTag, ((File) postElement).getDisplayName());
								super.onComponentTagBody(markupStream, openTag);
							}
						};
						
						FileElementPanel elementPanel = new FileElementPanel("elementPanel",fileLink,iconUrl);
						elementItem.add(elementPanel);
						
						elementPanel.add(new SimpleAttributeModifier("style", "padding-left: " + pixels + "px"));
					}
					catch (PersistenceException pe)
					{
						pe.printStackTrace();
					}
				}

				if (postElement instanceof LinkRule)
				{
					ExternalLink link = new ExternalLink("link", ((LinkRule) postElement).getUrl())
					{
						protected void onComponentTag(ComponentTag tag)
						{
							tag.setName("a");
							super.onComponentTag(tag);
						}

						protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
						{
							replaceComponentTagBody(markupStream, openTag, ((LinkRule) postElement).getDisplayName());
							super.onComponentTagBody(markupStream, openTag);
						}
					};

					PopupSettings popup = new PopupSettings(PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS);
					popup.setWidth(600);
					popup.setHeight(400);
					
					link.setPopupSettings(popup);
					
					FileElementPanel elementPanel = new FileElementPanel("elementPanel",link,"/library/image/silk/link.png");
					
					elementPanel.add(new SimpleAttributeModifier("style", "padding-left: " + pixels + "px"));
					
					elementItem.add(elementPanel);
				}
			}
		};

		add(postElements);
	}

	public void setEditMode(boolean editMode)
	{
		this.editMode = editMode;
	}
}
