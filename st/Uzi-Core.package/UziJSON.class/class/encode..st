encode
encode: anObject
	^ WebUtils jsonEncode: (self createJSONObject: anObject)