<?php
$where2 = $where.(($where != '') ? ' and ' : ' where ').' l."event" is not null and l."event" not in (\'getClientInformation\', \'loadFile\', \'showError\', \'HTTPError\')';
$query = 'select l."scene", l."event", count(*) as "amount" from "sivaPlayerCorrectedLog" l '.str_replace('s.', 'l.', $where2).' group by l."scene", l."event" order by l."scene" asc, l."event" asc';
$result = @pg_query($query) or throwMessage(pg_last_error());

$scenes = array();
foreach((array)$reportConfig[((int)$_GET['where']['videoId'][0])]['scenes'] as $scene){
	$scenes[($scene['id'])] = $scene;
}

?>
<div id="chart" class="treemapChart"></div>
<script src="/js/treemap.js" type="text/javascript"></script>
<script type="text/javascript">
var json = {
 "name": "flare",
 "children": [
   <?php
   $scene = -1;
   $i = 0;
   $j = 0;
   while($row = pg_fetch_assoc($result)){
	if($scene != $row['scene']){
		if($i != 0){
			echo ']},';
		}
		echo '{"name":"'.(($scenes[($row['scene'])]['name'] != '') ? $scenes[($row['scene'])]['name'] : $row['scene']).'"'.(($scenes[($row['scene'])]['fontColor'] != '') ? ',"fontColor":"#'.$scenes[($row['scene'])]['fontColor'].'"' : '').(($scenes[($row['scene'])]['backgroundColor'] != '') ? ',"backgroundColor":"#'.$scenes[($row['scene'])]['backgroundColor'].'"' : '').',"children":[';
		$scene = $row['scene'];
		$i++;
		$j = 0;
	}
	if(in_array($row['event'], array('useButton', 'clickVideo'))){
		$where2 = $where.(($where != '') ? ' and ' : ' where ').' l."scene" = '.pg_escape_literal($row['scene']).' and l."event" = '.pg_escape_literal($row['event']).' ';
		$query = 'select l."element", count(*) as "amount" from "sivaPlayerCorrectedLog" l '.str_replace('s.', 'l.', $where2).' group by l."element" order by l."element" asc';
		$result2 = @pg_query($query) or throwMessage(pg_last_error());
		while($row2 = pg_fetch_assoc($result2)){
			if($j > 0){
				echo ',';
			}
			echo '{"name":"'.$row['event'].' '.$row2['element'].' ('.$scenes[($row['scene'])]['name'].')","size":'.$row2['amount'].(($scenes[($row['scene'])]['fontColor'] != '') ? ',"fontColor":"#'.$scenes[($row['scene'])]['fontColor'].'"' : '').(($scenes[($row['scene'])]['backgroundColor'] != '') ? ',"backgroundColor":"#'.$scenes[($row['scene'])]['backgroundColor'].'"' : '').'}';
			$j++;
		}
		continue;
	}
	if($j > 0){
		echo ',';
	}
	echo '{"name":"'.$row['event'].' ('.(($scenes[($row['scene'])]['name'] != '') ? $scenes[($row['scene'])]['name'] : $row['scene']).')","size":'.$row['amount'].(($scenes[($row['scene'])]['fontColor'] != '') ? ',"fontColor":"#'.$scenes[($row['scene'])]['fontColor'].'"' : '').(($scenes[($row['scene'])]['backgroundColor'] != '') ? ',"backgroundColor":"#'.$scenes[($row['scene'])]['backgroundColor'].'"' : '').'}';
	$j++;
   }
   ?>
  ]}]};
createTreemapChart(json);
</script>