<?php 

class ZipFiles extends SyncFunction {
	function doExecute($config) {
		$temp = $config['tmp'] . '/' . uniqid() . ".zip";
		
		$root = $config['path'] . '/';
		
		$files = Array();
		$cnt = 0;
		while(true) {
			if (!isset($_REQUEST['path' . $cnt]))
				break;
			$path = sanitize_path_name($_REQUEST['path' . $cnt]);
			array_push($files, $path);
			$cnt++;
		}
		
		if (!create_zip($files, strlen($root), $temp, true)) {
			header($_SERVER["SERVER_PROTOCOL"].' 501 Internal server error');
			echo "Can't create zip file";
			if (is_file($temp))
				unlink($temp);
			exit;
		}
		
		header('Content-Type: application/zip' );
		readfile($temp);
		unlink($temp);
		
	}
}

class ZipExtension extends SyncExtension {
	function doInit(& $config) {
		$config['functions']['files'] = new ZipFiles();
	}
	
	function doFileInfo($config, $path) {
	}
	
}

/* creates a compressed zip file */
function create_zip($files = array(), $cut = 0 , $destination = '',$overwrite = false) {
	//if the zip file already exists and overwrite is false, return false
	if(file_exists($destination) && !$overwrite) { return false; }
	//vars
	$valid_files = array();
	//if files were passed in...
	if(is_array($files)) {
		//cycle through each file
		foreach($files as $file) {
			//make sure the file exists
			if(file_exists($file)) {
				$valid_files[] = $file;
			}
		}
	}
	//if we have good files...
	if(count($valid_files)) {
		//create the archive
		$zip = new ZipArchive();
		if($zip->open($destination,$overwrite ? ZIPARCHIVE::OVERWRITE : ZIPARCHIVE::CREATE) !== true) {
			return false;
		}
		//add the files
		foreach($valid_files as $file) {
			$zip->addFile($file, substr($file, $cut ) );
		}
		//debug
		//echo 'The zip archive contains ',$zip->numFiles,' files with a status of ',$zip->status;

		//close the zip -- done!
		$zip->close();

		//check to make sure the file exists
		return file_exists($destination);
	}
	else
	{
		return false;
	}
}

$config['extensions']['ZIP'] = new ZipExtension();

?>