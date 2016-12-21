requests
processRequest: aWebRequest
	| method |
	method := aWebRequest method asSymbol.
	actions at: method
		ifPresent: [:action | action cull: aWebRequest cull: self]
		ifAbsent: [aWebRequest send405Response: self allowedMethods]