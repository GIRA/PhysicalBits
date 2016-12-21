registry
removeUrl: urlString
	"Remove and answer the WebServer registered for a given URL string.
	If no WebServer was registered, return nil."

	^self removeUrl: urlString ifAbsent:[nil]