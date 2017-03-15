# mhus-hsync
Sync tools like rsync using http protocol.

A big problem of rsync is that it is not possible to synchrionize files over the very common protocol 'http' or 'https'. To solve the requirement I created this project. The goals should be synchronizing files over http using different programming languages. Therefore a simple protocol will do the job.

The tool consists of a (HTTP) server side and a client side. For both there will be different implementations in different languages but with the same protocol. In this way the software is compatible and able to work cross over.

Preferred server side language is PHP and Java (Servlet implementation). The first cliend side is Java but the job should also be done with Python. Maybe a C# implementation should be done also.

## Versions

Version 1: The HTTP side is only read only 

Version 2: Also implement at HTTP side write functionality

## PHP Sync-Server Installation

* Download the repository zip file
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

* enabled=true|**false**: Activate / Deactovate this repository
* description=text: Description of the repository
* public=true|**false**: Set to true if you do not need authorization (if you use a basic auth access control every user can access the repository)
* path=path: The path to the root of the repository to share
* users=Array of allowed users: If the repository is not public, a list of allowed user names
* showHidden=true|**false**: Enable show and follow hidden entries (starting with .)
* showLinks=**true**|false: Enable show and follow links
* followLinks=true|**false**: Enable following links

Server Extensions:
* Include extension file to enable it

Notes:

* IMPORTANT: In the server it could be possible to access all files in the repository, inclusive hidden and linked entries even if the will not be shown in the structure output.


## Java Client

Use the java client jar file to sync a repository [build/hsync.jar](https://github.com/mhus/mhus-hsync/raw/master/build/hsync.jar)

java -jar hsync.jar -url http://localhost/hsync/hsync.php -r test -d pull ~/tmp/test

If you need to compile the client before:
* clone sources: git clone https://github.com/mhus/mhus-hsync.git
* change directory into java server: cd mhus-hsync/java/server
* compile using maven: mvn install
* Use the compiled assembly: mhus-hsync/java/hsync-client/target/hsync-client-1.0.0-SNAPSHOT-jar-with-dependencies.jar

Options:
* -url url: The url to the server side script
* -u user: Username to login (if needed)
* -p password: Password to login
* -r repository: Name of the repository
* -notmodified: Do not use the CheckModified extension (will not recognice a change if only modify date is changed)
* -notsize: Do not use the CheckSize extension (will not recognice a change if the size of the file has changed)
* -overwrite: Do overwrite all files
* -d or -delete: Enable to delete files also (disabled by default)
* -extensions: A comma separated list of class pathes that should be used as extensions
* -v: verbose output
* -vv: more verbose output
* -perm: Use UnixPerm extension to sync also unix permissions. Server needs to use UnixPerms extension too
* -x: Additional parameters, only key to set to true or key=value, e.g -x checkmodified or -x password=abc

Parameters:
* command: clone|pull|info
* Local document root

Commands:
* clone: pull remote content and create a .hsync.properties file to mark the local repository project
* pull: pull data from remote. If a local repository is found, it will be done relative to the local repository
* info: print remote repository informations

Local Repository Project:
* The file .hsync.properties marks a local repository root
* The file contains the default repository connect configuration
* The command 'clone' will write this file and set all given parameters

Example:
```
mikehummel:~ # java -jar hsync.jar -url http://localhost/hsync/hsync.php -r test -d pull ~/tmp/test
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

If you have static content and need to initialize existing content, disable modified check and use the SetModifyDate extension:

```
java -jar hsync.jar -notmodified -extensions de.mhus.hsync.lib.client.ExtUpdateModifyDate pull

m /animal/links.txt
Pulled : 0, 0 Bytes
Deleted: 0, 0 Bytes
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

The server needs three main functions and one default extension (ZIP).

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
* link target (link:String) (only for links)

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
http://hsync?repository=test&function=file&path=/sample.pdf

(Content stream)
```
### (4) Download files (extension ZIP, optional)

* Function: files
* The etension is optional and depends on the implementation of the server side

In:
* repository name (repository:String)
* path0..n (path[0..n]:String)

Out:
* The files packed in a zip file as content

## Protocol Version 2

Aditional functionallity to allow write back operations.

* delete: Delete a file, directory or link
* mkdir: Create a directory
* upload: Upload a file
* link: create a link
* zipupload: Upload a package of files (ZIP extension, optional)

### (5) Delete node

Delete a file, directory (recursive) or link.

Function: delete

In:
* repository
* path

Out:
* result (success:boolean)

Example
```
http://hsync?repository=test&function=delete&path=/sample.pdf

{
  'success':true
}
```

### (6) Create remote directory

Function: mkdir

Create the full path of directories.

In:
* repository
* path

Out:
* Result (success:boolean)

### (7) Create or upload remote file

Function: upload

In:
* repository
* path
* (file content upload)

Out:
* result (sucess:boolean)

### (8) Create or update a link

Function: link

Create a new link. If the path already exists, it will be deleted. No matter file, directory or link.

In:
* repository
* path
* target

Out:
* Result (success:boolean)

### (9) Upload files using a zip archive for transfer

Function: zipupload

Upload a zip file and iport the content relative to the given path.

In:
* repository
* path
* (zip content file)

Out:
* Result (sucess:boolean)
