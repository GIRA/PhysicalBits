sending
sendResponse: resp contentBlock: contentBlock
	"Sends a WebResponse, streaming its contents from aStream.
	If a size is provided, insert a Content-Length header, otherwise
	ensure that the connection is transient."

	"Add Date and Server headers"
	resp headerAt: 'Date' put: (server ifNil:[WebServer]) serverDate.
	resp headerAt: 'Server' put: (server ifNil:[WebServer]) serverString.

	self closeIfTransient: resp after:[
		"Ignore all network errors while sending the response"
		[resp writeOn: stream.
		stream flush.
		self isHeadRequest ifFalse:[contentBlock value: stream].
		] on: NetworkError do:["ignore"].

		"Make sure we log before closing the connection"
		server ifNotNil:[server logRequest: self response: resp].
	].