if(!sivaPlayerInitated){
	var sivaPlayerInitated = 1;
	var sivaPlayerCurrentScript =  document.getElementsByTagName("script");
	var sivaPlayerPath = sivaPlayerCurrentScript[sivaPlayerCurrentScript.length - 1].src.split('/');
	var sivaPlayerInitParameters = sivaPlayerPath.pop();
	sivaPlayerPath.pop();
	sivaPlayerPath = sivaPlayerPath.join('/');	
	document.write('<link href="' + sivaPlayerPath + '/css/sivaPlayer.css" rel="stylesheet" type="text/css" />');
	document.write('<script src="' + sivaPlayerPath + '/js/frameworks/jquery.min.js" type="text/javascript"></script>');
	document.write('<script src="' + sivaPlayerPath + '/js/frameworks/jquery.mobile.min.js" type="text/javascript"></script>');
	document.write('<script src="' + sivaPlayerPath + '/js/frameworks/jquery.hashchange.min.js" type="text/javascript"></script>');
	document.write('<script src="' + sivaPlayerPath + '/js/frameworks/modernizr.min.js" type="text/javascript"></script>');
	document.write('<script src="' + sivaPlayerPath + '/js/frameworks/d3.v3.min.js" type="text/javascript"></script>');
	document.write('<script src="' + sivaPlayerPath + '/js/frameworks/linkify.min.js" type="text/javascript"></script>');
	document.write('<script src="' + sivaPlayerPath + '/js/frameworks/jquery.linkify.min.js" type="text/javascript"></script>');
	document.write('<script src="' + sivaPlayerPath + '/js/frameworks/fastclick.js" type="text/javascript"></script>');
	sivaPlayerInitParameters = sivaPlayerInitParameters.split('?');
	if(sivaPlayerInitParameters.length > 1){
		sivaPlayerInitParameters = sivaPlayerInitParameters[1].split('&');
		for(var i = 0; i < sivaPlayerInitParameters.length; i++){
			var sivaPlayerParam = sivaPlayerInitParameters[i].split('=');
			if(sivaPlayerParam[0] == 'lang' && sivaPlayerParam[1]){
				var sivaPlayerLanguages = sivaPlayerParam[1].split(',');
				for(var j = 0; j < sivaPlayerLanguages.length; j++){
					document.write('<script src="' + sivaPlayerPath + '/js/lang/' + sivaPlayerLanguages[j] + '.js" type="text/javascript"></script>');
				}
			}
		}
	}
	document.write('<script src="' + sivaPlayerPath + '/js/sivaPlayer.js" type="text/javascript"></script>');
}