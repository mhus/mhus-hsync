# mhus-hsync
Sync tools like rsync in different languages but with the same protocol over http

The tool consists of a server (HTTP) side and a client side. For both there will be different implementations in different languages but with the same protocol. In this way the software is compatible and able to work cross over.

## Versions

Version 1: The HTTP side is only read only 

Version 2: Also implement at HTTP side write functionality

## Installation

## Configuration

## Client



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

http://hsync?function=metadata&repository=test

{
  'version': 1,
  'description': 'A simple repository',
  'repository': 'test',
  'extensions': 'ZIP'
}

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

### (3) Download file

* Function: file

In:
* repository name (repository:String)
* path (path:String)

Out:
* The file as content

Example:

http://hsync?function=file&path=/sample.pdf

(Content stream)

### (4) Download files (extension ZIP)

* Function: files
* The etension is optional and depends on the implementation of the server side

In:
* repository name (repository:String)
* path0..n (path[0..n]:String)

Out:
* The files packed in a zip file as content



