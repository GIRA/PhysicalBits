examples
browseDir: directory request: request
	"Responds with a directory listing back to the original request"

	| entries listing url |
	url := request url.
	(url endsWith: '/') ifTrue:[url := url allButLast].

	entries := directory entries sort:[:e1 :e2|
		e1 isDirectory = e2 isDirectory 
			ifTrue:[e1 name <= e2 name]
			ifFalse:[e2 isDirectory]].

	listing := String streamContents:[:s|
		s nextPutAll:'<html><head><title>Index of ', request url,'</title></head><body>'.
		s nextPutAll:'<h1>Index of ', request url,'</h1>'.
		s nextPutAll:'<pre>      Name                      Last modified      Size  Description<hr>'.
		s nextPutAll: '[up]  <a href="', (url copyUpToLast: $/),'">parent</a>'; crlf.
		entries do:[:e|
			s nextPutAll:(e isDirectory ifTrue:['[dir] '] ifFalse:['      ']).
			s nextPutAll: '<a href="', url, '/', e name, '">'.
			s nextPutAll: (e name truncateTo: 30).
			s nextPutAll: '</a>'.
			e name size < 30 ifTrue:[s nextPutAll: (String new: 30 - e name size withAll: $ )].
			s nextPutAll: (e modificationDateAndTime asString padded: #right to: 20 with: $ ).
			s nextPutAll: (e fileSize asString padded: #right to: 20 with: $ ).
			s crlf.
		].
 	].

	request 
		send200Response: (listing convertToWithConverter: UTF8TextConverter new) 
		contentType: 'text/html; charset=utf-8'.

