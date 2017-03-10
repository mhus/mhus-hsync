# mhus-hsync
Sync tools like rsync in different languages but compatible over http.

A big problem of rsync is that it is not possible to synchrionize files over the very common protocol 'http' or 'https'. To solve the requirement I created this project. The goals should be synchronizing files over http in different languages. Therefore a simple protocol will do the job.

The tool consists of a server (HTTP) side and a client side. For both there will be different implementations in different languages but with the same protocol. In this way the software is compatible and able to work cross over.

## Versions

Version 1: The HTTP side is only read only 

Version 2: Also implement at HTTP side write functionality

## PHP Sync-Server Installation

* Copy the 'php/server/' parts into a PHP enabled document root and configure the repositories by creating files 'conf/'. 
* Remove the 'test' repsoitory (repo_test.php). 
* Remove test folder 'repo'. 
* Set a .htaccess and .htpasswd files to enable authentication.

If you have the preositories inside the document root set also strong access rules to this repository. If you want to use repositories outside the document root you need to change the php rules. The same for temp directories.

Test the installation (modify url and repository name):
* http://localhost/hsync/hsync.php?repository=test&function=metadata
* http://localhost/hsync/hsync.php?repository=test&function=structure

## PHP Sync-Server Configuration

For every repository create a repo_name of the repo.php file in the conf folder. Copy the repo_sample.php file. Open the file and modify the configuration parameters.

Parameters:

* enabled=true|false: Activate / Deactovate this repository
* description=text: Description of the repository
* public=true|false: Set to true if you do not need authorization (if you use a basic auth access control every user can access the repository)
* path=path: The path to the root of the repository to share
* users=Array of allowed users: If the repository is not public, a list of allowed user names

## Java Client

Use the java client jar file to sync a repository

java -jar hsync.jar -url http://localhost/hsync/hsync.php -r test -d pull ~/tmp/test

If you need to compile the client before:
* clone sources: git clone https://github.com/mhus/mhus-hsync.git
* change directory into java server: cd mhus-hsync/java/server
* compile using maven: mvn install
* Use the compiled assembly: mhus-hsync/java/hsync-client/target/hsync-client-1.0.0-SNAPSHOT-jar-with-dependencies.jar

You need to set:
* -url url: The url to the server side script
* -u user: Username to login (if needed)
* -p password: Password to login
* -r repository: Name of the repository
* command: pull|info
* Local document root

You can set:
* -d: delete local files if not needed
* -v: verbose output
* -vv: more verbose output

Example:
```
mikehummel:~ # java -jar mhus-hsync/java/hsync-client/target/hsync-client-1.0.0-SNAPSHOT-jar-with-dependencies.jar -url http://localhost/hsync/hsync.php -r test -d pull ~/tmp/test
+ d /animal
+ d /animal/amphibians
+ d /animal/birds
+ d /animal/fish
+ d /animal/invertebrates
+ d /animal/invertebrates/arthropods
+ f /animal/invertebrates/arthropods/insects.txt
+ f /animal/invertebrates/snail.txt
+ f /animal/links.txt
+ d /animal/mammals
+ d /animal/reptiles
+ d /vehicle
+ d /vehicle/auto
+ d /vehicle/plain
```

## Protocol transport layer

The transport layer is based on the HTTP protocol.

In:
* Given parameters via HTTP parameters per GET or POST x-www-form-urlencoded. Do not mix it!
* For every function definition there can be additional input parameters, depending of the extensions

Out data:
* As simple JSON structure with content type application/json. First element is ever a object node {}
* For every function definition there can be additional output parameters, depending of the extensions

Out content:
* As pure content stream using a typicall content type.

Out error:
* By default the server will return the HTTP 200 retur code
* If the return code is not 200 an error occured (see HTTP specification)

## Protocol Version 1

The server need tree main functions and one default extension (ZIP).

### (1) A metadata request to get the current server information and also check the accessibility

* Function: metadata

In:
* repository name (repository:String)

Out:
* protocol version (version:int)
* repository description (description:String)
* repository name (repository:String)
* extensions (extensions:String): Comma separated list of installed extensions, the list is case insensitive 

Example:
```
http://hsync?function=metadata&repository=test

{
  'version': 1,
  'description': 'A simple repository',
  'repository': 'test',
  'extensions': 'ZIP'
}
```
### (2) Request structure

* Function: structure
* The root node has an empty name

In:
* repository name (repository:String)
* root path (optional) (path:String)
* younger then (optional) (modified:long)

Out: A list of files in a deep array structure, every node is an object:
* name (name:String)
* type (type:String - f:file/d:directory)
* modifyDate (modified:long) (optional for directories)
* size (size:long) (in bytes, files only)
* children (nodes:Array) (only for directories)

Example:
```
http://hsync?function=structure&repository=test&path=/

{
  'name' : '',
  'type' : 'd',
  'nodes' : [
    {
      'name': 'sample.pdf',
      'type': 'f',
      'modified': 1488971686869,
      'size': 26278372
    },
    {
      'name': 'subdir',
      'type': 'd',
      'nodes': [
        ...
      ]
    }
  ]
}
```
### (3) Download file

* Function: file

In:
* repository name (repository:String)
* path (path:String)

Out:
* The file as content

Example:
```
http://hsync?function=file&path=/sample.pdf

(Content stream)
```
### (4) Download files (extension ZIP)

* Function: files
* The etension is optional and depends on the implementation of the server side

In:
* repository name (repository:String)
* path0..n (path[0..n]:String)

Out:
* The files packed in a zip file as content



