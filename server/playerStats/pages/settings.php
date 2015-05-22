<?php
$site['title'] = 'Settings';
$site['script'] = '<link href="/css/chosen.min.css" rel="stylesheet" type="text/css" />
		<script src="/js/chosen.jquery.min.js" type="text/javascript"></script>';
$fields = array(
	'db_host' => 'Database Host',
	'db_port' => 'Database Port',
	'db_user' => 'Database User',
	'db_pass' => 'Database Password',
	'db_name' => 'Database Name'
);

$message = '';
if(isset($_POST['submit'])){
	$errors = array();
	$config['users'] = array();
	foreach((array)$_POST['users'] as $user){
		$config['users'][] = (int)$user;		
	}
	foreach($_POST as $key => $val){
		if($fields[$key] == ''){
			unset($_POST[$key]);
		}
	}
	foreach($fields as $field => $description){
		if(trim($_POST[$field]) == ''){
			$errors[] = 'Please provide the <i>'.$description.'</i>.';
		}
	}
	if(count($errors) == 0){
		$config = array_merge($config, array('db_name' => $_POST['db_name'], 'db_host' => $_POST['db_host'], 'db_port' => $_POST['db_port'], 'db_user' => $_POST['db_user'], 'db_pass' => $_POST['db_pass']));
		if(!establishDatabaseConnection(true)){
			$errors[] = 'Could not connect to database.';
		}
	}
	if(count($errors) == 0){
		$open = fopen(dirname(__FILE__).'/config.txt', 'w+');
		fwrite($open, json_encode($config));
		fclose($open);
		@pg_query('CREATE TABLE IF NOT EXISTS "sivaPlayerCorrectedLog" ("entry" INT, "session" INT, "user" INT, "video" INT, "videoVersion" INT, "scene" VARCHAR(255), "event" VARCHAR(255), "element" VARCHAR(255), "additionalInformation" VARCHAR(255), "sceneTimeOffset" FLOAT, "time" TIMESTAMP WITH TIME ZONE, "clientTime" BIGINT, "staticSessionId" VARCHAR(30))') or throwMessage(pg_last_error());
		$message = createMessage('Changes successfully saved.', 'confirm');
	}
	else{
		$message = createMessage(implode('<br />', $errors));
	}
}
?>
<h1><?=$site['title']?></h1>
<?=$message?>
<form action="" method="post">
 <?foreach($fields as $field => $description):?>
  <div class="row">
   <label for="<?=$field?>"><?=$description?>:</label>
   <input type="<?=((preg_match('!_pass!', $field)) ? 'password' : 'text')?>" id="<?=$field?>" name="<?=$field?>" value="<?=((isset($_POST[$field])) ? $_POST[$field] : $config[$field])?>" />
  </div>
 <?endforeach;?>
 <?if(establishDatabaseConnection(true)):?>
 <div class="row">
  <label for="excluded_users">Stats accessable for:</label>
  <?php 
  $row = pg_fetch_assoc(@pg_query('select "id", "title", "firstName", "lastName", "email" from "user" where "deletable" = false limit 1'));
  ?>
  Main administrator <i><?=$row['lastName']?>, <?=$row['firstName']?>, <?=$row['title']?> (<?=$row['email']?>)</i> and ...
  <select name="users[]" id="users" style="width:100%;height:400px;" data-placeholder="Choose Users..." multiple>
   <?php
   $result = @pg_query('select "id", "title", "firstName", "lastName", "email" from "user" where "deletable" = true order by lower("lastName") asc, lower("firstName") asc, lower("title") asc, lower("email") asc') or throwMessage(pg_last_error());
   while($row = pg_fetch_assoc($result)){
		?>
		<option value="<?=$row['id']?>" <?=((in_array($row['id'], (array)$config['users'])) ? 'selected="selected"' : '')?>><?=$row['lastName']?>, <?=$row['firstName']?>, <?=$row['title']?> (<?=$row['email']?>)</option>
		<?php
   }
   ?>
  </select>
 </div>
 <?endif;?>
 <div class="row">
  <input type="submit" name="submit" value="Save Settings" />
 </div>
 <script type="text/javascript">	
  	$(document).ready(function(){
  		$('#users').chosen();
  	});
 </script>
</form>