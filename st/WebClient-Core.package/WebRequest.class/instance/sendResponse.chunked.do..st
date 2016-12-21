responses
sendResponse: code chunked: chunkBlock do: hdrBlock
	"Send a chunked response"

	| resp |
	resp := self newResponse protocol: 'HTTP/1.1' code: code.
	resp headerAt: 'Transfer-Encoding' put: 'chunked'.
	hdrBlock value: resp.
	^self sendResponse: resp contentBlock:[:aStream|
		"Set the stream in the request and pass it in the chunk block"
		resp stream: aStream.
		chunkBlock value: resp.
		"send termination chunk"
		aStream nextPutAll: '0'; crlf; crlf; flush.
	].
