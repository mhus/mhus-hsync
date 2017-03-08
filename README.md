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

Out data:
* As simple JSON structure with content type application/json. First element is ever a object node {}

Out content:
* As pure content stream using a typicall content type.

## Protocol Version 1

The server need tree main functions and one default extension (ZIP).

### (1) A metadata request to get the current server information and also check the accessibility

In:
* archive name (archive:String)

Out:
* protocol version (version:String)
* archive description (description:String)
* archive name (archive:String)
* extensions (extensions:String): Comma separated list of installed extensions, the list is case insensitive 

### (2) Request structure

In:
* archive name (archive:String)
* root path (optional) (path:String)
* younger then (optional) (modified:long)

Out: A list of files in a deep array structure, every node is an object:
* name (name:String)
* type (type:String - f:file/d:directory)
* modifyDate (modified:long) (optional for directories)
* size (size:long) (in bytes)
* children (nodes:Array) (only for directories)

### (3) Download file

In:
* archive name (archive:String)
* path (path:String)

Out:
The file as content

### (4) Download files (extension ZIP)

In:
* archive name (archive:String)
* path0..n (path[0..n]:String)

Out:
The files packed in a zip file as content



