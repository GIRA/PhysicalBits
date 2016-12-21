registry
removeUrl: urlString ifAbsent: aBlock
	"Remove and answer the WebServer registered for a given URL string."

	^self registry removeKey: urlString ifAbsent: aBlock