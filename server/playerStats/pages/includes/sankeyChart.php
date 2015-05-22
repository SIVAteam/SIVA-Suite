<?php
$nodes = array();
$nodesInverse = array();
$sessions = array();
$where .= (($where != '') ? ' and ' : ' where ').' l."type" = \'loadScene\' ';
$query = 'select l."session" as "session", l."element" as "node" from "sivaPlayerLog" l left join "sivaPlayerSession" s on(l."session" = s."id") '.$where.' order by l."session" asc, l."time" asc';
$result = @pg_query($query) or throwMessage(pg_last_error());
$i = 0;
$prefixSession = 0;
$prefix = '';
while($row = pg_fetch_assoc($result)){
	if($prefixSession != $row['session']){
		$prefixSession = $row['session'];
		$prefix = '';
	}
	if(!isset($nodesInverse[($prefix.'_'.$row['node'])])){
		$nodes[$i] = $prefix.'_'.$row['node'];
		$nodesInverse[($prefix.'_'.$row['node'])] = $i;
		$i++;
	}
	$node = $nodesInverse[($prefix.'_'.$row['node'])];
	$sessions[($row['session'])][] = $node;
	$prefix .= $node.',';
}
$end = $i;
$nodes[$end] = 'SessionEnd';
$nodesInverse['SessionEnd'] = $íd;
$links = array();
foreach($sessions as $id => $s){
	foreach($s as $k => $e){
		$t = $s[$k + 1];
		if(isset($t)){
			$links[($e)][($t)]++;
		}
		else{
			$links[($e)][$end]++;
		}
	}
	unset($sessions[$id]);
}
?>
<div id="chart" class="sankeyChart"></div>
<script src="/js/sankey.js"></script>
<script type="text/javascript">
var legend = [];
var colors = {};
<?php
foreach((array)$reportConfig[((int)$_GET['where']['videoId'][0])]['scenes'] as $scene){
	echo 'legend.push({\'id\':\''.$scene['id'].'\', \'name\': \''.$scene['name'].'\'});';
	echo 'colors[\''.$scene['id'].'\'] = {\'font\':\'#'.$scene['fontColor'].'\', \'background\': \'#'.$scene['backgroundColor'].'\'};';
}
?>
var data = {"nodes":[<?php
for($i = 0; $i < count($nodes); $i++){
?>
{'name':'<?=preg_replace('!(^|(.*),)_!', '', $nodes[$i])?>'}<?=(($i < count($nodes) - 1) ? ',' : '')?>
<?php
}
?>],
"links":[<?php
for($i = 0; $i < count($links); $i++){
	$j = 0;
	foreach($links[$i] as $target => $amount){
?>
{"source":<?=$i?>,"target":<?=$target?>,"value":<?=$amount?>}<?=(($i < count($links) - 1 or $j < count($links[$i]) - 1) ? ',' : '')?>
<?php
	$j++;
	}
}
?>]};
createSankeyChart(data);
</script>