<?php
function addPath($paths, $newPath, $level = 0){
	if(count($newPath) > $level){
		$paths[($newPath[$level])] = addPath($paths[($newPath[$level])], $newPath, $level + 1);
	}
	else{
		if($paths[-1])
			$paths[-1]++;
		else
			$paths[-1] = 1;
	}
	return $paths;
}

function pathsToJSON($paths){
	global $end;
	$i = 0;
	$length = count($paths);
	foreach($paths as $k => $p){
		$i++;
		if($k == -1){
			continue;
		}
		if($k === 'end'){
			$k = $end;
		}
		echo '{\'name\':'.$k;
		if(count($p) > 1 or !isset($p[-1])){
			echo ',\'children\':[';
			pathsToJSON($p);
			echo ']';
		}
		if(isset($p[-1])){
			echo ',\'size\':'.$p[-1];
		}
		echo '}';
		if($i < $length){
			echo ',';
		}
	}
}

$where .= (($where != '') ? ' and ' : ' where ').' l."event" = \'loadScene\' ';
$query = str_replace('s.', 'l.', 'select l."session" as "session", l."element" as "node" from "sivaPlayerCorrectedLog" l '.$where.' order by l."session" asc, l."time" asc');
$result = @pg_query($query) or throwMessage(pg_last_error());
$i = 0;
$paths = array();
$session = 0;
$pathPrefix = array();
$nodes = array();
$nodesInverse = array();
while($row = pg_fetch_assoc($result)){
	if(!isset($nodesInverse[($row['node'])])){
		$nodes[$i] = $row['node'];
		$nodesInverse[($row['node'])] = $i;
		$i++;
	}
	if($session != $row['session']){
		if($session > 0){
			$pathPrefix[] = 'end';
			$paths = addPath($paths, $pathPrefix);
		}
		$pathPrefix = array();
		$session = $row['session'];
	}
	$pathPrefix[] = $nodesInverse[($row['node'])];
}
$end = $i;
$nodes[$end] = 'SessionEnd';
$nodesInverse['SessionEnd'] = $end;
$paths = addPath($paths, $pathPrefix);
?>
<div id="chart" class="sunburstChart">
 <div id="sequence"></div>
 <div id="explanation" style="visibility: hidden;">
  <span id="percentage"></span><br/>
  of paths with this sequence of scenes
 </div>
 <div id="legend"></div>
</div>
<script src="/js/sunburst.js" type="text/javascript"></script>
<script type="text/javascript">
var legend = [];
var colors = {};
<?php
foreach((array)$reportConfig[((int)$_GET['where']['videoId'][0])]['scenes'] as $pos => $scene){
	if(in_array($scene['id'], $nodes)){
		echo 'legend['.$nodesInverse[($scene['id'])].'] = {\'id\':\''.$scene['id'].'\', \'name\': \''.$scene['name'].'\', \'position\' : '.($pos + 1).'};';
		echo 'colors[\''.$scene['id'].'\'] = {\'font\':\'#'.$scene['fontColor'].'\', \'background\': \'#'.$scene['backgroundColor'].'\'};';
		$key = array_keys($nodes, $scene['id']);
		unset($nodes[($key[0])]);
	}
}
foreach($nodes as $n){
	echo 'legend['.$nodesInverse[$n].'] = {\'id\':\''.$n.'\'};';
}
?>
var data = {'name': 'root', 'children': [<?=pathsToJSON($paths)?>]};
createSunburstChart(data);
</script>