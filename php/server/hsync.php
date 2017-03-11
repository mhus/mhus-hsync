<?php

require_once 'functions/api.php';
require_once 'functions/metadata.php';
require_once 'functions/structure.php';
require_once 'functions/file.php';

// default headers
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Credentials: true');
header('Access-Control-Allow-Headers: Content-Type');
header('Access-Control-Allow-Methods: GET,POST');

// init default configuration
$config['enabled'] = false;
$config['private'] = true;
$config = Array();
$config['functions'] = Array();
$config['functions']['metadata'] = new FunctionMetadata();
$config['functions']['structure'] = new FunctionStructure();
$config['functions']['file'] = new FunctionFile();
$config['extensions'] = Array();
include 'conf/defaults.php';

// load repo configuration
$repo = $_REQUEST['repository'];
$config['name'] = $repo;

require_once 'conf/repo_' . sanitize_file_name($repo) . ".php";

// check if repo exists and is enabled
if (!$config['enabled']) {
	header($_SERVER["SERVER_PROTOCOL"]." 404 Not Found", true, 404);
	echo "Repository not found";
	exit;
}

// check if user is allowed
if ($config['private']) {
	if (!isset($_SERVER['PHP_AUTH_USER'])) {
		header('WWW-Authenticate: Basic realm="'.$config['realm'].'"');
		header($_SERVER["SERVER_PROTOCOL"].' 401 Unauthorized');
		echo "User not set";
		exit;
	}
	$user = $_SERVER['PHP_AUTH_USER'];
	if (!isset($config['users'][$user])) {
		header('WWW-Authenticate: Basic realm="'.$config['realm'].'"');
		header($_SERVER["SERVER_PROTOCOL"].' 401 Unauthorized');
		echo "Unauthorized";
		exit;
	}
}

// init extensions
foreach ( $config['extensions'] as $key => $value ) {
	if ($value instanceof SyncExtension) {
		$value->doInit($config);
	}
}

// check function
$func = $_REQUEST['function'];
if (!isset($config['functions'][$func]) || !($config['functions'][$func] instanceof SyncFunction) ) {
	header($_SERVER["SERVER_PROTOCOL"].' 501 Internal server error');
	echo "Function unknown";
	exit;
}

if ($_SERVER['REQUEST_METHOD'] == "GET" || $_SERVER['REQUEST_METHOD'] == "POST" ) {

	// execute function
	$config['functions'][$func]->doExecute($config);

}

exit;

function sanitize_file_name( $filename ) {
//	return preg_replace("/[^a-zA-Z0-9\.]/", "", strtolower($filename));
	$dangerous_characters = array(" ", '"', "'", "&", "/", "\\", "?", "#", "*");
	return str_replace($dangerous_characters, '_', $filename);
}

function sanitize_path_name( $filename ) {
	//	return preg_replace("/[^a-zA-Z0-9\.\/]/", "", strtolower($filename));
	$dangerous_characters = array(" ", '"', "'", "&", "?", "#", "*");
	return str_replace($dangerous_characters, '_', $filename);
}

function validate_link_target($target) {
	if (
			strlen($target) < 1 ||
			$target[0] == '.' ||
			$target[0] == '~' ||
			$target[0] == '/' ||
			strpos($target, '..') > -1 ||
			strpos($target, ':') > -1
			) return false;
			return true;
}

?>