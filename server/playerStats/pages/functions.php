<?php
class DatabaseException extends Exception{}

function createMessage($text, $cssClass = 'error'){
	$msg = '<div class="'.$cssClass.'Message">'.(($cssClass == 'error') ? '<b>Error:</b> ' : '').$text.'</div>';
	if($echo)
		echo $msg;
	else
		return $msg;
}
function throwMessage($text){
	throw new DatabaseException($text);
}
function establishDatabaseConnection($test = false){
	global $config;
	$connection = @pg_connect('host='.$config['db_host'].' port='.$config['db_port'].' dbname='.$config['db_name'].' user='.$config['db_user'].' password='.$config['db_pass']) or $test or throwMessage('Could not establish database connection. Please check the database settings.');
	return (boolean)$connection;
}
function createDataTableRow($array, $isHeaderRow = false){
	$tmp = '<tr '.(($isHeaderRow) ? 'class="header"' : '').'>';
	foreach($array as $value){
		$tmp .= '<td>'.$value.'</td>';
	}
	$tmp .= '</tr>';
	return $tmp;
}
function generateDataTableQueryString($params){
	foreach($params as $key => $value){
		if($key == 'page' or $key == 'submit' or $key == 'csv'){
			unset($params[$key]);
		}
		else{
			if(is_array($value)){
				foreach($value as $key2 => $value2){
					if(is_array($value2)){
						foreach($value2 as $key3 => $value3){
							$params[$key.'['.$key2.']['.$key3.']'] = urlencode($key.'['.$key2.'][]').'='.urlencode($value3);
						}
					}
					else{
						$params[$key.'['.$key2.']'] = urlencode($key.'['.$key2.']').'='.urlencode($value2);
					}
				}
				unset($params[$key]);
			}
			else{
				$params[$key] = urlencode($key).'='.urlencode($value);
			}
		}
	}
	return implode('&amp;', $params);
}
?>