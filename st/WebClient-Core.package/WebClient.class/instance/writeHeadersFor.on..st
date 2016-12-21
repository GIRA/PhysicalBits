sending
writeHeadersFor: request on: aStream
	"Write all the headers for the given request on aStream"

	request headersDo:[:key :value| aStream nextPutAll: key, ': ', value asString; crlf].
