<?php
include(dirname(__FILE__).'/config.php');
include(dirname(__FILE__).'/functions.php');

$limitInMinutes = 15;

establishDatabaseConnection();
?>
<h1>Database Update</h1>
<?php
@pg_query('TRUNCATE TABLE "sivaPlayerCorrectedLog"') or throwMessage(pg_last_error());

foreach((array)$reportConfig as $video => $value){
        $reportConfig[$video]['excluded_sessions'] = explode(',', $value['excluded_sessions']);
}

$userSessions = array();
function writeLogEntries($id, $entries){
	global $sessionRewriting, $userSessions, $reportConfig;
	$userSessions[($entries[0]['user'])]++;
	if(!in_array($entries[0]['user'], (array)$reportConfig[($entries[0]['video'])]['excluded_users']) and !in_array($entries[0]['user'].'-'.$userSessions[($entries[0]['user'])], (array)$reportConfig[($entries[0]['video'])]['excluded_sessions'])){
		$cols = array('entry', 'user', 'video', 'videoVersion', 'sceneTimeOffset', 'clientTime');
		foreach($entries as $row){
			foreach($cols as $col){
				if($row[$col] == ''){
					$row[$col] = 'null';
				}
			}
			@pg_query('INSERT INTO "sivaPlayerCorrectedLog" VALUES ('.$row['entry'].', '.$id.', '.$row['user'].', '.$row['video'].', '.$row['videoVersion'].', '.pg_escape_literal($row['scene']).', '.pg_escape_literal($row['event']).', '.pg_escape_literal($row['element']).', '.pg_escape_literal($row['additionalInformation']).', '.$row['sceneTimeOffset'].', '.pg_escape_literal($row['time']).', '.$row['clientTime'].', '.pg_escape_literal($row['user'].'-'.$userSessions[($row['user'])]).')') or doError($row, $entries, pg_last_error());	
		}
	}
}

function doError($row, $entries, $error){
	var_dump($row);
	var_dump($entries);
	throwMessage($error);
}


$serverLog = array();
$log = array();
$cols = array('videoVersion', 'event', 'element', 'additionalInformation', 'sceneTimeOffset', 'clientTime');
$result = @pg_query('SELECT "entry", "session", "user", "video", "videoVersion", "scene", "event", "element", "additionalInformation", "sceneTimeOffset", "time", "clientTime" FROM "sivaPlayerLogByScene" ORDER BY "video" ASC, "user" ASC, "clientTime" ASC, "session" ASC, "scene" ASC, "event" ASC, "element" ASC, "additionalInformation" ASC') or throwMessage(pg_last_error());
while($row = pg_fetch_assoc($result)){
	if($row['clientTime'] == 0){
		if(!is_array($serverLog[($row["session"])])){
			$serverLog[($row["session"])] = array();
		}
		$serverLog[($row["session"])][] = $row;
	}
	else{
		$hash = '';
		foreach($cols as $col){
			$hash .= $row[$col];
		}
		$hash = md5($hash);
		if(!is_array($log[($row['video'])])){
			$log[($row['video'])] = array();
		}
		if(!is_array($log[($row['video'])][($row['user'])])){
			$log[($row['video'])][($row['user'])] = array();
		}
		$log[($row['video'])][($row['user'])][$hash] = $row;
	}
}

$i = 1;
$sessions = array();
$sessionRewriting = array();
foreach($log as $video => $v){
	foreach($v as $user => $u){
		$clientTime = 0;
		foreach($u as $row){
			if($clientTime < $row['clientTime'] - 1000 * 60 * $limitInMinutes){
				if($clientTime > 0 ){
					$sessions[$i] = $session;
					$i++;
				}
				$session = array();
				foreach((array)$serverLog[($row['session'])] as $serverEvent){
					$session[] = $serverEvent;
				}
			}
			$clientTime = $row['clientTime'];
			$session[] = $row;
			$sessionRewriting[($row['session'])] = $i;
		}
		$sessions[$i] = $session;
		$i++;
	}
}

foreach($sessions as $id => $session){
	$delete_session = true;
	foreach($session as $row){
		if(!in_array($row['event'], array('loadScene', 'getClientInformation', 'showError', 'leaveVideo', 'openFork'))){
			$delete_session = false;
			break;
		}
	}
	if(!$delete_session){
		writeLogEntries($id, $session);
	}
}

pg_close();
?>