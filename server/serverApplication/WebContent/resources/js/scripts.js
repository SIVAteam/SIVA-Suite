function hasToScrollToContent() {
	var element = document.getElementById('topLink');
	
	if (window.getComputedStyle(element, null).getPropertyValue('display') == 'none') {
		scrollToContent();
	}
}

var lastY = -1;

function scrollToContent() {
	var contentY = document.getElementById('content').offsetTop;
	var scrollDistance = contentY - window.scrollY;

	if (lastY != window.scrollY && scrollDistance > 0) {
		lastY = window.scrollY;

		if (scrollDistance > 50) {
			scrollDistance = 50;
		}

		window.scrollBy(0, scrollDistance);
		setTimeout('scrollToContent()', 50);
	}
}

function onloadJs(){
	hasToScrollToContent();
	focusFirstFormField();
	prepareLoginRedirect();
}

function focusFirstFormField(){
	var focusField = undefined;
	var fields;
	
	fields = document.getElementById('content').getElementsByTagName('input');
	for(var i = 0; i < fields.length; i++) {
		if(fields[i].type == 'text' || fields[i].type == 'password') {
			
			focusField = fields[i];
			break;
		}		
	}
	
	fields = document.getElementById('content').getElementsByTagName('textarea');
	if(fields.length > 0) {
		if(focusField == undefined || fields[0].offsetTop < focusField.offsetTop) {
			focusField = fields[0];
		}
	}	
	
	if(focusField != undefined) {
		document.getElementById(focusField.id).focus();
	}
}

function prepareLoginRedirect(){
	var redirectURL = document.getElementById('loginForm:redirect');
	if(redirectURL && redirectURL.value == ''){
		redirectURL.value = window.location.href;
	}
	redirectURL = document.getElementById('headerLoginForm:redirect');
	if(redirectURL && redirectURL.value == ''){
		redirectURL.value = window.location.href;
	}
}