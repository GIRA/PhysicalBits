as yet unclassified
parse: jsonString
	^ self createObject: (WebUtils jsonDecode: jsonString readStream)