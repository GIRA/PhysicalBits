registry
forUrl: urlString ifAbsent: aBlock
	"Answer the WebServer registered for a given URL string.
	If no WebServer is registered, evaluate aBlock."

	^self registry at: urlString ifAbsent: aBlock