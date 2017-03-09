<?php 

class FunctionMetadata extends SyncFunction {
	function doExecute($config) {
		$arr = Array();
		$arr['version'] = 1;
		$arr['description'] = $config['description'];
		$arr['repository'] = $config['name'];
		$arr['extensions'] = join(',', array_keys($config['extensions']) );
		$arr['functions'] = join(',', array_keys($config['functions']) );
		$arr['name'] = $config['name'];
		echo json_encode($arr);
	}
}
?>