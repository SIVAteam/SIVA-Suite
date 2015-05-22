<?php
$orderBy = array();
foreach((array)$_GET['sort'] as $col => $direction){
	if($direction == 'ASC' or $direction == 'DESC'){
		$orderBy[] = pg_escape_identifier($col).' '.$direction;
	}
}
$orderBy = implode(', ', $orderBy);
if($orderBy != ''){
	$orderBy = 'order by '.$orderBy.' ';
}
$offset = 0;
if($_GET['offset'] != ''){
	$offset = (int)$_GET['offset'];
}
$limit = 50;
if($_GET['limit'] != ''){
	$limit = (int)$_GET['limit'];
	$_SESSION['limit'] = $limit;
}
else if($_SESSION['limit'] != ''){
	$limit = $_SESSION['limit'];
}
$where = array();
if(is_array($_POST['where'])){
	$_GET['where'] = array_merge((array)$_GET['where'], $_POST['where']);
}
if(count($_POST) == 0){
	unset($_GET['where']['id']);
}
foreach((array)$_GET['where'] as $col => $value){
	if($whereCols[$col] != '' and $value != ''){
		if(is_array($value)){
			$tmp = array();
			foreach($value as $value2){
				$tmp[] = $whereCols[$col].' = '.pg_escape_literal($value2);
			}
			$where[] = '('.implode(' or ', $tmp).')';
		}
		else{
			if($col == 'id'){
				$tmp = explode(',', $value);
				if(count($tmp) == 0)
					continue;
				foreach($tmp as $k => $t){
					$tmp[$k] = (int)$t;
				}
				$where[] = $whereCols[$col].' in ('.implode(',', $tmp).')';
			}
			else{
				$where[] = $whereCols[$col].' = '.pg_escape_literal($value);
			}
		}
	}
}
foreach(array('from', 'to') as $param){
	preg_replace('!([^0-9-]+)!i', '', $_GET[$param]);
	if($_GET[$param] != '' and count(explode('-', $_GET[$param])) == 3){
		$where[] = 'l."time" '.(($param == 'from') ? '>=' : '<=').' '.pg_escape_literal($_GET[$param].' '.(($param == 'from') ? '00:00:00' : '23:59:59'));
	}
}
$tmp = array();
foreach((array)$reportConfig as $video => $value){
	if(count($value['excluded_users']) > 0){
		$where[] = '(s."video" != '.$video.' OR s."user" NOT IN ('.implode(',', $value['excluded_users']).'))';
	}
	$value['excluded_sessions'] = explode(',', $value['excluded_sessions']);
	foreach($value['excluded_sessions'] as $key => $value2){
		$value['excluded_sessions'][$key] = '\''.$value2.'\''; 
	}
	$value['excluded_sessions'] = implode(',', $value['excluded_sessions']);
	if($value['excluded_sessions'] != ''){
		$where[] = '(s."video" != '.$video.' OR s."staticSessionId" NOT IN ('.$value['excluded_sessions'].'))';
	}
}
$where = implode(' and ', $where);
if($where != ''){
	$where = 'where '.$where;
}
?>