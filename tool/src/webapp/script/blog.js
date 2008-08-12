/*
 * Copyright 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
*/

function checkInputOnKeyUp(input, textToCheck)
{
	if (input.value=='')
		input.value=textToCheck;
	if (input.value!=textToCheck)
		input.style.color='#000000';
	else
		input.style.color='#CCCCCC';
}

function checkInputOnKeyPress(input, textToCheck)
{
	if (input.value==textToCheck)
		input.value='';
}

function confirmPostDelete()
{
	return confirm('Are you sure you want to delete the post?');
}

function confirmPostElementDelete()
{
	return confirm('Are you sure you want to delete the post element?');
}

function moveProfilePopup(e)
{
	$("#profilePopup").css({ left:e.clientX + 15,top:e.clientY }); 
}

function hideProfilePopup(e)
{
	//$("#profilePopup").css({ visibility:"hidden" }); 
	//$("#profilePopup").css({ display:"none" }); 
	$("#profilePopup").fadeOut("fast"); 
}

function showProfilePopup(e)
{
	//$("#profilePopup").css({ visibility:"visible" }); 
	$("#profilePopup").fadeIn("fast");
}

function showTimeoutPanel(e)
{
	$("#timeoutPanel").slideDown(); 
}

function hideTimeoutPanel(e)
{
	$("#timeoutPanel").slideUp(); 
}

function confirmCommentDelete()
{
	return confirm('Are you sure you want to delete the comment?');
}

function launchFullSizeImage(fileName,url)
{
	window.open(url,fileName,"width=400,height=300,status=no,resizable=yes,location=no,scrollbars=yes");
}