registry
forUrl: urlString
	"Answer the WebServer registered for a given URL string."

	^self forUrl: urlString ifAbsent:[self newForUrl: urlString]