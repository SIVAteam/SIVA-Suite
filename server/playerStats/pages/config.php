<?php
$config = array(
	'navigation' => array(
		array('text' => 'Home', 'url' => '/home.php'),
		array('text' => 'Player Sessions', 'url' => '/playerSessions.php'),
		array('text' => 'Player Session Tracking', 'url' => '/playerSessionTracking.php'),
		array('text' => 'Report Settings', 'url' => '/reportSettings.php'),
		array('text' => 'Settings', 'url' => '/settings.php'),
		array('text' => 'Logout', 'url' => '/logout.php')
	)
);

$tmpConfig = json_decode(@file_get_contents(dirname(__FILE__).'/config.txt'), true);
$config = array_merge($config, (array)$tmpConfig);

$reportConfig = json_decode(@file_get_contents(dirname(__FILE__).'/reportConfig.txt'), true);
?>