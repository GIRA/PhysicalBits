examples
browseFile: file request: request
	"Responds with a file back to the original request"

	| fileSize mimeTypes resp |
	file binary.
	fileSize := file size.
	mimeTypes := file mimeTypes ifNil:[#('application/octet-stream')].
	resp := request newResponse protocol: 'HTTP/1.1' code: 200.
	resp headerAt: 'Content-Type' put: mimeTypes first.
	request sendResponse: resp contentStream: file size: fileSize.