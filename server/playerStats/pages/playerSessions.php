<?php
$site['title'] = 'Player Sessions';
$site['script'] = '<link href="/css/chosen.min.css" rel="stylesheet" type="text/css" />
		<script src="/js/chosen.jquery.min.js" type="text/javascript"></script>';
establishDatabaseConnection();
?>
<h1>Player Sessions</h1>
<?php
$whereCols = array(
	'videoId' => 's."video"',
	'userId' => 's."user"',
	'id' => 's."session"'
);
if(!isset($_GET['sort'])){
	$_GET['sort']['sessionId'] = 'ASC';
}
include(dirname(__FILE__).'/includes/dataTableQueryStrings.php');

$query = 'select s."session" as "sessionId", s."user" as "userId", concat(u."title", \' \', u."firstName", \' \', u."lastName") as "userName", u."email" as "userEmail", s."video" as "videoId", s."videoVersion", v."title" as "videoTitle", date_trunc(\'second\', MIN(s."time")) as "sessionStart", date_trunc(\'second\', (COALESCE(MAX(s."time"), MIN(s."time")) - MIN(s."time"))) as "sessionDuration" from "sivaPlayerCorrectedLog" s left join "user" u on(s."user" = u."id") left join "video" v on(s."video" = v."id") '.$where.' group by s."session", s."user", u."title", u."firstName", u."lastName", u."email", s."video", s."videoVersion", v."title" '.$orderBy.((isset($_POST['csv'])) ? '' : ' OFFSET '.$offset.' LIMIT '.$limit);
$query_amount = 'select count(distinct s."session") as amount from "sivaPlayerCorrectedLog" s '.$where;

if(isset($_POST['csv'])){
	$result = @pg_query($query) or throwMessage(pg_last_error());
	$open = fopen(dirname(__FILE__).'/../exports/sessions.csv', 'w+');
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
    header('Content-Disposition:attachment;filename=sessions.csv');
    header('Content-Length:'.filesize(dirname(__FILE__).'/../exports/sessions.csv'));
    ob_clean();
    flush();
    readfile(dirname(__FILE__).'/../exports/sessions.csv');
	die();
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
   <input type="submit" name="submit" value="Apply" />
  </div>
 </div>
</form>
<form action="" method="post">
 <div class="col">
  <div class="row">
   <label>&nbsp;</label>
   <input type="hidden" id="whereId" name="where[id]" value="" />
   <input type="submit" name="csv" value="Generate CSV File" />
  </div>
 </div>
 <script type="text/javascript">
  $(document).ready(function(){
		$('#where_eventType, #where_videoId, #where_userId').chosen();
  });
 </script>
</form>
<?php
$tmp = $_GET;
unset($tmp['offset']);
unset($tmp['sort']);
unset($tmp['limit']);
$linked_cols = array('sessionId' => '/playerSessionTracking.html?where[sessionId]={value}');
$tmp['where']['userId'] = '{value}';
$linked_cols['userId'] = '?'.generateDataTableQueryString($tmp);
unset($tmp['where']['userId']);
$tmp['where']['videoId'] = '{value}';
$linked_cols['videoId'] = '?'.generateDataTableQueryString($tmp);
include(dirname(__FILE__).'/includes/dataTables.php');

pg_close();
?>