utilities
htmlSubmit: urlString fields: fieldMap method: method
	"A utility method for html submit operations. The fieldMap can be EITHER
	an array of associations OR a Dictionary of key value pairs (the former is
	useful for providing multiple fields and/or specifying the order of fields).

		WebClient 
			htmlSubmit: 'http://www.google.com/search'
			fields: {
				'hl' -> 'en'.
				'q' -> 'Squeak'
			} method: 'GET'
	"
	^self htmlSubmit: urlString 
			fields: fieldMap
			method: method
			encoding: 'application/x-www-form-urlencoded'
