<?php
session_start();
session_regenerate_id();
include(dirname(__FILE__).'/pages/config.php');
include(dirname(__FILE__).'/pages/functions.php');

$page = (($_GET['page'] != '') ? $_GET['page'] : 'home.php');
if(!file_exists(dirname(__FILE__).'/pages/'.$page)){
	$page = 'error404.php';
}
if($_SESSION['id'] == ''){
	if(file_exists(dirname(__FILE__).'/pages/config.txt'))
		$page = 'login.php';
	else
		$page = 'settings.php';
}

$site = array();
ob_start();
try{
	include(dirname(__FILE__).'/pages/'.$page);
}
catch(DatabaseException $e){
	echo createMessage($e->getMessage());
}
$site['content'] = ob_get_contents();
ob_end_clean();
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="de">
 <head>
  <title><?=$site['title']?></title>
  <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
  <link rel="icon" href="/images/favicon.ico" type="image/x-icon" />
  <link rel="stylesheet" href="/css/style.css" type="text/css" />
  <link rel="stylesheet" href="/css/jquery-ui.min.css" type="text/css" />
  <script src="/js/jquery.min.js" type="text/javascript"></script>
  <script src="/js/jquery-ui.min.js" type="text/javascript"></script>
  <script src="/js/scripts.js" type="text/javascript"></script>
  <?=$site['script']?>
 </head>
 <body>
  <div id="header">
   <div id="logo">SIVA Player <span>Stats</span></div>
  </div>
  <div id="navigation">
   <?foreach($config['navigation'] as $link):?>
    <a href="<?=preg_replace('!\.php$!', '.html', $link['url'])?>" <?=(('/'.$page == $link['url']) ? 'class="current"' : '')?>><?=$link['text']?></a>
   <?endforeach;?>
  </div>
  <div id="content">
   <?=$site['content']?>
  </div>
  <div id="footer">
   &copy; Copyright 2014 by University of Passau, <a href="http://siva.uni-passau.de" target="_blank">SIVA Player Project</a>
  </div>
 </body>
</html>