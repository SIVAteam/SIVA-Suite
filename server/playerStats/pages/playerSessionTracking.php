<?php
$site['title'] = 'Player Session Tracking';
$site['script'] = '<link href="/css/chosen.min.css" rel="stylesheet" type="text/css" />
		<script src="/js/chosen.jquery.min.js" type="text/javascript"></script>';
establishDatabaseConnection();
?>
<h1>Player Session Tracking</h1>
<?php
if(isset($_GET['allSessions'])){
	unset($_GET['where']);
}
$whereCols = array(
	'videoId' => 's."video"',
	'userId' => 's."user"',
	'sessionId' => 'l."session"',
	'eventType' => 'l."event"',
	'id' => 'l."id"'
);
if(!isset($_GET['sort'])){
	$_GET['sort']['entryId'] = 'ASC';
}
$contains_log_table = true;
include(dirname(__FILE__).'/includes/dataTableQueryStrings.php');
			
$query = str_replace('s.', 'l.', 'select l."entry" as "entryId", l."session" as "sessionId", l."user" as "userId", l."video" as "videoId", l."videoVersion", l."scene", l."event" as "eventType", l."element" as "eventElement", l."additionalInformation" as "eventExtraInfo", l."sceneTimeOffset" as "sceneTime", l."clientTime" as "eventTime" from "sivaPlayerCorrectedLog" l '.$where.' group by l."entry", s."session", s."user", s."video", s."videoVersion", l."scene", l."event", l."element", l."additionalInformation", l."sceneTimeOffset", l."clientTime" '.$orderBy.((isset($_POST['csv'])) ? '' : ' OFFSET '.$offset.' LIMIT '.$limit));
$query_amount = str_replace('s.', 'l.', 'select count(l."entry") as "amount" from "sivaPlayerCorrectedLog" l '.$where);

if(isset($_POST['csv'])){
	$result = @pg_query($query) or throwMessage(pg_last_error());
	$open = fopen(dirname(__FILE__).'/../exports/tracking.csv', 'w+');
	$i = 0;
	while($row = pg_fetch_assoc($result)){
		if($i == 0){
			fputcsv($open, array_keys($row), ';', '"');
		}
		fputcsv($open, $row, ';', '"');
		$i++;
	}
	fclose($open);
	
	header('Content-Type:text/csv;charset=utf-8');
    header('Content-Disposition:attachment;filename=tracking.csv');
    header('Content-Length:'.filesize(dirname(__FILE__).'/../exports/tracking.csv'));
    ob_clean();
    flush();
    readfile(dirname(__FILE__).'/../exports/tracking.csv');
	die();
}
else if(isset($_POST['diagrams'])){
	include(dirname(__FILE__).'/playerSessionDiagrams.php');
	return;
}

$result = @pg_query($query_amount) or throwMessage(pg_last_error());
$amount = pg_fetch_assoc($result);
$tmp = $_GET;
unset($tmp['offset']);
unset($tmp['sort']);
unset($tmp['limit']);
?>
<form action="?<?=generateDataTableQueryString($tmp)?>" method="get" class="border">
  <div class="col">
  <div class="row">
   <label for="where_eventType">Event Type:</label>
   <select name="where[eventType][]" id="where_videoId" data-placeholder="Choose..." multiple>
	<?php
	$result = @pg_query('select distinct "type" from "sivaPlayerLog" order by "type"') or throwMessage(pg_last_error());
	while($row = pg_fetch_assoc($result)){
		?>
		<option value="<?=$row['type']?>" <?=((in_array($row['type'], (array)$_GET['where']['eventType'])) ? 'selected="selected"' : '')?>><?=$row['type']?></option>
		<?php
	}
	?>
   </select>
  </div>
 </div>
 <?if($_GET['where']['sessionId'] == ''):?>
 <div class="col">
  <div class="row">
   <label for="where_videoId">Video:</label>
   <select name="where[videoId][]" id="where_videoId" data-placeholder="Choose..." multiple>
	<?php
	$result = @pg_query('select "id", "title" from "video" order by "title"') or throwMessage(pg_last_error());
	while($row = pg_fetch_assoc($result)){
		?>
		<option value="<?=$row['id']?>" <?=((in_array($row['id'], (array)$_GET['where']['videoId'])) ? 'selected="selected"' : '')?>><?=$row['title']?></option>
		<?php
	}
	?>
   </select>
  </div>
 </div>
 <div class="col">
  <div class="row">
   <label for="where_userId">User:</label>
   <select name="where[userId][]" id="where_userId" data-placeholder="Choose..." multiple>
	<?php
	$result = @pg_query('select "id", concat("title", \' \', "firstName", \' \', "lastName") as "userName", "email" from "user" order by "userName"') or throwMessage(pg_last_error());
	while($row = pg_fetch_assoc($result)){
		?>
		<option value="<?=$row['id']?>" <?=((in_array($row['id'], (array)$_GET['where']['userId'])) ? 'selected="selected"' : '')?>><?=$row['userName']?> (<?=$row['email']?>)</option>
		<?php
	}
	?>
   </select>
  </div>
 </div>
 <div class="col">
  <div class="row">
   <label>&nbsp;</label>
   <input type="hidden" id="from" name="from" value="<?=$_GET['from']?>" />
   <input type="hidden" id="to" name="to" value="<?=$_GET['to']?>" />
   <input type="hidden" id="offset" name="offset" value="0" />
   <input type="hidden" id="limit" name="limit" value="<?=$_GET['limit']?>" />
   <input type="submit" name="apply" value="Apply" />
  </div>
 </div>
 <script type="text/javascript">
  $(document).ready(function(){
		$('#where_eventType, #where_videoId, #where_userId').chosen();
  });
 </script>
 <?else:?>
 <?php
 $result = @pg_query('select concat(u."title", \' \', u."firstName", \' \', u."lastName") as "userName", u."email" as "userEmail", v."title" as "videoTitle", date_trunc(\'second\', s."start") as "sessionStart", date_trunc(\'second\', (COALESCE(max(l."time"), s."start") - s."start")) as "sessionDuration" from "sivaPlayerSession" s left join "user" u on(s."user" = u."id") left join "video" v on(s."video" = v."id") left join "sivaPlayerLog" l on (s."id" = l."session") where s."id" = '.pg_escape_literal($_GET['where']['sessionId']).' group by s."id", u."title", u."firstName", u."lastName", u."email", v."title", s."start" LIMIT 1') or throwMessage(pg_last_error());
 $session = pg_fetch_assoc($result);
 ?>
 <div class="col tiny">
  <div class="row">
   <label for="where_videoId">Session Start:</label>
   <?=$session['sessionStart']?>
  </div>
 </div>
 <div class="col tiny">
  <div class="row">
   <label for="where_videoId">Session Duration:</label>
   <?=$session['sessionDuration']?>
  </div>
 </div>
 <div class="col tiny">
  <div class="row">
   <label for="where_videoId">Video:</label>
   <?=$session['videoTitle']?>
  </div>
 </div>
 <div class="col tiny">
  <div class="row">
   <label for="where_userId">User:</label>
   <?if($session['userEmail'] != ''):?><?=$session['userName']?> (<?=$session['userEmail']?>)<?else:?>Anonymous<?endif;?>
  </div>
 </div>
 <div class="col border">
  <div class="row">
   <label>&nbsp;</label>
   <input type="hidden" name="where[sessionId]" value="<?=$_GET['where']['sessionId']?>" />
   <input type="submit" name="apply" value="Apply" />
   <input type="submit" name="allSessions" value="Show All Sessions" />
  </div>
 </div>
 <?endif;?>
</form>
<form action="" method="post">
 <div class="col">
  <div class="row">
   <label>&nbsp;</label>
   <input type="hidden" id="whereId" name="where[id]" value="" />
   <input type="submit" name="csv" value="Generate CSV File" />
   <input type="submit" name="diagrams" value="Generate Diagrams" />
  </div>
 </div>
</form>
<?php
$tmp = $_GET;
unset($tmp['offset']);
unset($tmp['sort']);
unset($tmp['limit']);
$linked_cols = array('sessionId' => '?where[sessionId]={value}');
$tmp['where']['userId'] = '{value}';
$linked_cols['userId'] = '?'.generateDataTableQueryString($tmp);
unset($tmp['where']['userId']);
$tmp['where']['videoId'] = '{value}';
$linked_cols['videoId'] = '?'.generateDataTableQueryString($tmp);
include(dirname(__FILE__).'/includes/dataTables.php');

pg_close();
?>