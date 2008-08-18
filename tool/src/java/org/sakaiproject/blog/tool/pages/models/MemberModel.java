package org.sakaiproject.blog.tool.pages.models;

import org.apache.wicket.model.IModel;
import org.sakaiproject.blog.tool.BlogApplication;
import org.sakaiproject.blog.api.BlogMember;

public class MemberModel implements IModel
{
	private String memberId;

	private transient BlogMember member;

	public MemberModel(BlogMember member)
	{
		this.member = member;
		this.memberId = member.getUserId();
	}

	public MemberModel(String memberId)
	{
		this.memberId = memberId;
	}

	public Object getObject()
	{
		// If member is null this model may have just been serialized. Reconstruct
		// it from storage
		if (member == null)
		{
			// TODO: We need to setup the state of member fully !
			member = BlogApplication.get().getSakaiProxy().getMember(memberId);
		}

		return member;
	}

	public void setObject(Object object)
	{
		if (object instanceof BlogMember)
			member = (BlogMember) object;
		else
			System.out.println("Failed to setObject on this MemberModel. The object needs to be a Member.");
	}

	public void detach()
	{
		member = null;
	}
}
