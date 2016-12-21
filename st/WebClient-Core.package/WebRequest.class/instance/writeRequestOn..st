printing
writeRequestOn: aStream
	"Write the request"

	aStream nextPutAll: method; space; nextPutAll: rawUrl; space; nextPutAll: protocol; crlf.
