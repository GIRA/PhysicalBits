initialize
readFrom: aStream
	"Initialize the receiver initialized from an existing socket stream 
	This method will block until the headers have been parsed."

	| tokens |
	stream := aStream.
	stream isBinary ifTrue:[stream ascii].
	"Parse request header"
	request := stream upToAll: String crlf.
	tokens := request findTokens: ' '.
	tokens size < 2 ifTrue:[^self error: 'Invalid HTTP request: ', request].
	method := tokens first.
	rawUrl := tokens second.
	tokens size > 2 
		ifTrue:[protocol := tokens third]
		ifFalse:[protocol := 'HTTP/1.0'].

	"Parse HTTP header"
	headers := WebUtils readHeadersFrom: stream.
