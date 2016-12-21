printing
writeStatusOn: aStream
	"Store this response on aStream"

	aStream nextPutAll: status; crlf.
