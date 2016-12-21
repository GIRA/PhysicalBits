printing
writeHeadersOn: aStream
	"Write the request"

	self headersDo:[:key :value| aStream nextPutAll: key, ': ', value asString; crlf].
