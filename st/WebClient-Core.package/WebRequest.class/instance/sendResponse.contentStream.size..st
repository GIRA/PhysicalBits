sending
sendResponse: resp contentStream: aStream size: streamSize
	"Sends a WebResponse, streaming its contents from aStream.
	If a size is provided, insert a Content-Length header, otherwise
	ensure that the connection is transient."

	streamSize 
		ifNil:[self headerAt: 'Connection' put: 'close'] "mark transient"
		ifNotNil:[resp headerAt: 'Content-Length' put: streamSize].

	^self sendResponse: resp contentBlock:[:sockStream|
		resp streamFrom: aStream to: sockStream size: streamSize progress: nil
	]