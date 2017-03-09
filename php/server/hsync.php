<?php

include 'functions/api.php';
include 'functions/metadata.php';
include 'functions/structure.php';
include 'functions/file.php';

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

include 'conf/repo_' . sanitize_file_name($repo) . ".php";

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
	return $filename;
}

function sanitize_path_name( $filename ) {
	return $filename;
}

?>