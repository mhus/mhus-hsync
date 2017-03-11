<?php 

class FunctionStructure extends SyncFunction {
	private $showHidden;
	private $showLinks;
	private $followLinks;
	private $beautify = false;
	private $modified = 0;
	private $root;
	private $config;
	
	function doExecute($config) {
		
		if (isset($_REQUEST['modified']))
			$this->modified = intval($_REQUEST['modified']);
		if (isset($_REQUEST['beautify']))
			$this->beautify = boolval($_REQUEST['beautify']);
					
		$this->showHidden = $config['showHidden'];
		$this->showLinks = $config['showLinks'];
		$this->followLinks = $config['followLinks'];
		$this->root = $config['path'] . '/';
		$this->config = $config;
		$path = '';
		if (isset($_REQUEST['path']))
			$path = sanitize_path_name($_REQUEST['path']);
		$depth = 100;
		if (isset($_REQUEST['depth']))
			$depth = intval($_REQUEST['depth']);
		$path = $this->root . $path;
		
		if (!file_exists($path)) {
			header($_SERVER["SERVER_PROTOCOL"]." 404 Not Found", true, 404);
			echo "File not found";
			exit;
		}
		
		header('Content-Type: application/json');
		
		echo '{';
		if ($this->beautify) echo "\n";
		$this->printChildren($path,$depth);
		echo '}';
		if ($this->beautify) echo "\n";
		
	}
	
	function printFileInfo($path) {
		if (is_file($path)) {
			echo '"size" :' . filesize($path) . ",";
			if ($this->beautify) echo "\n";
			echo '"modified":' . filemtime($path) . ",";
			if ($this->beautify) echo "\n";
			echo '"type":"f",';
			if ($this->beautify) echo "\n";
		}
		if (is_link($path)) {
			echo '"target":' . json_encode(readlink($path)) . ',';
		}
		if (is_dir($path)) {
			echo '"type":"d",';
			if ($this->beautify) echo "\n";
		}
		if ($path == $this->root)
			echo '"name":"",';
		else
			echo '"name":' . json_encode( basename($path) ) . ',';
		if ($this->beautify) echo "\n";
		
		foreach ( $this->config['extensions'] as $key => $value ) {
			if ($value instanceof SyncExtension) {
				$value->doFileInfo($this->config,$path);
			}
		}
		
	}
		
	function printChildren($path,$depth) {
		
		$this->printFileInfo($path);

		if (!$this->followLinks && is_link($path)) return;
		
		$depth = $depth - 1;
		if ($depth < 0) return;
		
		if (is_dir($path)) {
			echo '"nodes": [';
			
			$scan_result = scandir($path);
			foreach ( $scan_result as $key => $value ) {
				// ignore the twins
				if ($value == '.' || $value == '..')
					continue;
				// childs path
				$p = $path . "/" . $value;
				// ignore hidden 
				if ($p[0] == '.' && !$this->showHidden)
					continue;
				// ignore links if not allowed or target not exists
				if (is_link($p)) {
					if(!$this->showLinks && is_link($p))
						continue;
 					$target = readlink($p);
 					if (!validate_link_target($target))
 						continue;
				}
				// ignore if modifydate is set as filter
				if ($this->modified > 0 && is_file($p) && filemtime($p) <= $this->modified )
					continue;
				// output for this element
				echo '{';
				if ($this->beautify) echo "\n";
				$this->printChildren($p, $depth);
				echo '},';
				if ($this->beautify) echo "\n";
					
			}
			
			echo "],";
			if ($this->beautify) echo "\n";
		}
		
	}
}
?>