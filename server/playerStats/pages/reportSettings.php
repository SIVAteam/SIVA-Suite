<?php
$site['title'] = 'Report Settings';
$site['script'] = '<script src="/js/extensibleForm.js" type="text/javascript"></script>
		<link href="/css/colorpicker.css" rel="stylesheet" type="text/css" />
		<script src="/js/colorpicker.js" type="text/javascript"></script>
		<link href="/css/chosen.min.css" rel="stylesheet" type="text/css" />
		<script src="/js/chosen.jquery.min.js" type="text/javascript"></script>';
establishDatabaseConnection();

$message = '';
if(isset($_POST['submit'])){
	$errors = array();
	$data = array();
	$data['scenes'] = array();
	foreach((array)$_POST['scenes'] as $scene){
		if($scene['id'] != ''){
			$data['scenes'][((int)$scene['position'])] = array(
				'id' => str_replace(array('"', '\''), '', $scene['id']),
				'name' => str_replace(array('"', '\''), '', $scene['name']),
				'fontColor' => preg_replace('!([^a-zA-Z0-9]+)!', '', $scene['fontColor']),
				'backgroundColor' => preg_replace('!([^a-zA-Z0-9]+)!', '', $scene['backgroundColor']),
			);
		}
	}
	ksort($data['scenes']);
	$data['excluded_users'] = array();
	foreach((array)$_POST['excluded_users'] as $user){
		$data['excluded_users'][] = (int)$user;
	}
	$data['excluded_sessions'] = array();
	$data['excluded_sessions'] = preg_replace('!([^0-9,-]+)!', '', $_POST['excluded_sessions']);
	$reportConfig[((int)$_GET['videoId'])] = $data;
	if(count($errors) == 0){
		$open = fopen(dirname(__FILE__).'/reportConfig.txt', 'w+');
		fwrite($open, json_encode($reportConfig));
		fclose($open);
		$message = createMessage('Changes successfully saved.', 'confirm');
	}
	else{
		$message = createMessage(implode('<br />', $errors));
	}
}
else
	$_POST = $reportConfig[((int)$_GET['videoId'])];
?>
<h1>Report Settings</h1>
<?if($_GET['videoId'] == ''):?>
<p>Please choose the video for editing its report settings.</p>
<div class="list">
<?php
$result = @pg_query('select "id", "title" from "video" order by "title"') or throwMessage(pg_last_error());
while($row = pg_fetch_assoc($result)){
	?>
	<div><a href="?videoId=<?=$row['id']?>"><?=$row['title']?></a></div>
	<?php
}
?>
</div>
<?else:?>
<?=$message?>
<form action="" method="post">
 <div class="row">
  <table id="scenes" class="dataTable settingTable">
   <tr class="header">
    <td>Scene ID</td>
    <td>Scene Name</td>
    <td>Scene Font Color</td>
    <td>Scene Background Color</td>
    <td></td>
   </tr>
   <?php
   $i = 1;
   foreach((array)$_POST['scenes'] as $scene){
   	?>
   	<tr class="extensibleFormRow">
     <td><input type="text" name="scenes[<?=$i?>][id]" class="extensibleFormCondition" value="<?=$scene['id']?>" /></td>
     <td><input type="text" name="scenes[<?=$i?>][name]" value="<?=$scene['name']?>" /></td>
     <td class="color"><input type="text" name="scenes[<?=$i?>][fontColor]" value="<?=$scene['fontColor']?>" /></td>
     <td class="color"><input type="text" name="scenes[<?=$i?>][backgroundColor]" value="<?=$scene['backgroundColor']?>" /></td>
     <td><input type="hidden" name="scenes[<?=$i?>][position]" value="<?=$i?>" class="position" /><span class="extensibleFormRemove" title="Delete">X</span></td>
    </tr>
   	<?php
   	$i++;
   }	
   ?>
   <tr class="extensibleFormRow extensibleFormTemplate">
    <td><input type="text" name="scenes[0][id]" class="extensibleFormCondition" value="" placeholder="eg. NodeScene_1" /></td>
    <td><input type="text" name="scenes[0][name]" value="" placeholder="eg. Introduction" /></td>
    <td class="color"><input type="text" name="scenes[0][fontColor]" value="000000" /></td>
    <td class="color"><input type="text" name="scenes[0][backgroundColor]" value="FFFFFF" /></td>
    <td><input type="hidden" name="scenes[0][position]" value="<?=$i?>" class="position" /><span class="extensibleFormRemove" title="Delete">X</span></td>
   </tr>
  </table>
 </div>
 <div class="row">
  <label for="excluded_users">Excluded Users:</label>
  <select name="excluded_users[]" id="excluded_users" style="width:100%;height:400px;" data-placeholder="Choose Users..." multiple>
   <?php
   $result = @pg_query('select "id", concat("title", \' \', "firstName", \' \', "lastName") as "userName", "email" from "user" order by "userName"') or throwMessage(pg_last_error());
   while($row = pg_fetch_assoc($result)){
		?>
		<option value="<?=$row['id']?>" <?=((in_array($row['id'], (array)$_POST['excluded_users'])) ? 'selected="selected"' : '')?>><?=$row['userName']?> (<?=$row['email']?>)</option>
		<?php
   }
   ?>
  </select>
 </div>
 <div class="row">
  <label for="excluded_sessions">Excluded Sessions (comma-separated):</label>
  <textarea name="excluded_sessions" style="width:100%;height:400px;"><?=$_POST['excluded_sessions']?></textarea>
 </div>
 <div class="row">
  <input type="submit" name="submit" value="Save Report Settings" />
 </div>
</form>
<script type="text/javascript">	
  	$(document).ready(function(){
  		$('#excluded_users').chosen();
  		$('#scenes').sortable({
			'items': 'tr:not(.extensibleFormNewRow)',
			'update': function(){
				setPositions();
			}
		});
		$('#scenes').extensibleForm({
			'onRebuild': function(){
				setPositions();
				$.each($('#scenes .extensibleFormRow:visible .color'), function(){
					var color = $('input', this).val();
					$('input', this).hide(0);
					$('span', this).remove();
					$(this).append($('<span></span>').css('backgroundColor', '#' + color));
					$(this).ColorPicker({
						'color': color,
						'onShow': function(colpkr){
							$(colpkr).fadeIn(500);
							return false;
						},
						'onHide': function(colpkr){
							$(colpkr).fadeOut(500);
							return false;
						},
						'onChange': function(element, hsb, hex, rgb){
							$('input', element).val(hex);
							$('span', element).css('backgroundColor', '#' + hex);
						}
					});
				});
			}
		});
	});

	function setPositions(){
		var i = 0;
		$.each($('#scenes .extensibleFormRow'), function(){
			$('.position', this).val(i);
			i++;
		});
	}
 </script>
<?endif;?>
<?php
pg_close();
?>