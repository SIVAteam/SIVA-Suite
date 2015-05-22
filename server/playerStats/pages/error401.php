<?php
header("HTTP/1.0 401 Unauthorized");
$site['title'] = 'Unauthorized';
?>
<h1><?=$site['title']?></h1>
You are not allowed to use this page without authorization.