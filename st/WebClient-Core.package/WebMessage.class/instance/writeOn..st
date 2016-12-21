printing
writeOn: aStream
	"Write the request"

	self writeHeadersOn: aStream.
	aStream crlf.
