accessing
serverString
	"Returns the server string to be used in responses.
	The response is cached for efficiency."

	^serverString ifNil:[serverString := self class serverString].