decode
decode: jsonString
	^ self createObject: (WebUtils jsonDecode: jsonString readStream)