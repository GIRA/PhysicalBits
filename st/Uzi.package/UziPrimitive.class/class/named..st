instance creation
named: primitiveName
	^ primitives 
		at: primitiveName 
		ifAbsent: [UziPrimitiveNotFoundError signal: primitiveName]
	