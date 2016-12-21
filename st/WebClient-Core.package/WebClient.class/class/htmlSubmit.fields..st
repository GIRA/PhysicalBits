utilities
htmlSubmit: urlString fields: fieldMap
	"A utility method for html submit operations. The fieldMap can be EITHER
	an array of associations OR a Dictionary of key value pairs (the former is
	useful for providing multiple fields and/or specifying the order of fields).

		WebClient 
			htmlSubmit: 'http://www.google.com/search'
			fields: {
				'hl' -> 'en'.
				'q' -> 'Squeak'
			}
	"
	^self htmlSubmit: urlString 
			fields: fieldMap
			method: 'GET'
