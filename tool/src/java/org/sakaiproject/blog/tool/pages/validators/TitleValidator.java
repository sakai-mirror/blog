package org.sakaiproject.blog.tool.pages.validators;

import org.apache.wicket.validation.INullAcceptingValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.AbstractValidator;

public class TitleValidator extends AbstractValidator implements INullAcceptingValidator
{
	@Override
	protected void onValidate(IValidatable validatable)
	{
		String title = (String) validatable.getValue();
		
		if(title == null || title.length() < 4 || title.length() > 255)
			validatable.error((new ValidationError()).setMessage("The title must be between 4 and 255 characters."));
	}
	
	public boolean validateOnNullValue()
	{
		return true;
	}
}
