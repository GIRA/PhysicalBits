accessing
requestLine
	"The actual request line as sent by the client"

	^request ifNil:[self method, ' ', self rawUrl, ' ', self protocol]