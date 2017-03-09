<?php 

abstract class SyncFunction {
	abstract function doExecute($config);
}

abstract class SyncExtension {
	abstract function doInit(& $config);
	abstract function doFileInfo($config, $path);
}

?>