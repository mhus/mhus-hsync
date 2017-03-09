<?php 

class FunctionFile extends SyncFunction {
	function doExecute($config) {
		
		$root = $config['path'] . '/';
		$path = '';
		if (isset($_REQUEST['path']))
			$path = sanitize_path_name($_REQUEST['path']);
		$depth = 100;
		if (isset($_REQUEST['depth']))
			$depth = intval($_REQUEST['depth']);
		$path = $root . $path;

		if (!file_exists($path)) {
			header($_SERVER["SERVER_PROTOCOL"]." 404 Not Found", true, 404);
			echo "File not found";
			exit;
		}
		
		header('Content-Type: ' . mime_content_type($path) );
		
		readfile($path);

	}
}
?>