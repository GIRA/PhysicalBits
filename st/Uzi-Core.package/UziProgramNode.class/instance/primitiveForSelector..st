accessing
primitiveForSelector: selector
	^ primitivesDict at: selector
		ifPresent: [:prim | prim name]
		ifAbsent: [nil]