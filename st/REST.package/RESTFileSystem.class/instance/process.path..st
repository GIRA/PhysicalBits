requests
process: request path: path
	| dir fileName |
	request isGetRequest 
		ifFalse: [request send405Response: self allowedMethods. ^ true].
	path ifEmpty: [^ false].
	
	fileName := path last.
	
	dir := path allButLast
		inject: directory
		into: [:subTotal :next | subTotal directoryNamed: next ].

	(dir exists and: [dir fileExists: fileName])
		ifFalse: [^ false].
	
	dir 
		fileNamed: fileName 
		do: [:file || fileSize contentType resp |
			file binary.
			fileSize := file size.
			contentType := self contentTypeFor: file.
			resp := request newResponse protocol: 'HTTP/1.1' code: 200.
			resp headerAt: 'Content-Type' put: contentType.
			request sendResponse: resp contentStream: file size: fileSize].
	^ true