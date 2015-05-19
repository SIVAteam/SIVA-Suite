function checkKey(email, key, secretKey){
	email = email.replace(/([^0-9a-zA-Z]+)/g, '').toLowerCase();
	var tmpKey = [];
	for(var i = 0; i < secretKey.length; i++){
		tmpKey[(i % key.length)] = secretKey.charAt((email.charAt(i % email.length).charCodeAt(0) + i) % secretKey.length);
	}
	var tmpKeyString = '';
	for(var i = 0; i < tmpKey.length; i++){
		tmpKeyString += tmpKey[i];
	}
	console.log(key, tmpKey, tmpKeyString);
	if(key == tmpKeyString){
		alert('OK');
	}
	else{
		alert('FAIL');
	}
}

checkKey('admin@handschigl.com', 'zt9gfutlg6u309PLq5a4e1v7gt4c', 'qaywsx741edcrfv852tgbzhn963ujmik0olpLPMKO369NJIBHUV258GZCFTXD147RYSE0AWQ');