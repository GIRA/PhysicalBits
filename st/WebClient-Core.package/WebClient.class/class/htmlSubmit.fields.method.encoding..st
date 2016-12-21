utilities
htmlSubmit: urlString fields: fields method: method encoding: encoding
	"A utility method for html submit operations. The fieldMap can be EITHER
	an array of associations OR a Dictionary of key value pairs (the former is
	useful for providing multiple fields and/or specifying the order of fields).

		WebClient 
			htmlSubmit: 'http://www.google.com/search'
			fields: {
				'hl' -> 'en'.
				'q' -> 'Squeak'
			} method: 'GET'
			encoding: 'application/x-www-form-urlencoded'
	"

	method = 'GET' ifTrue:[
		"GET only supports url encoded requests"
		encoding = 'application/x-www-form-urlencoded' 
			ifFalse:[^self error: 'Unsupported encoding: ', encoding].
		^self httpGet: urlString, '?', (WebUtils encodeUrlEncodedForm: fields).
	].

	method = 'POST' ifTrue:[
		"Dispatch on encoding type"
		encoding caseOf: {
			[ 'application/x-www-form-urlencoded'] -> [
				^self httpPost: urlString
					content: (WebUtils encodeUrlEncodedForm: fields)
					type: encoding.
			].
			['multipart/form-data'] -> [
				^self httpPost: urlString multipartFields: fields
			].
		} otherwise:[]
	].

	self error: 'Unsupported method: ', method.
