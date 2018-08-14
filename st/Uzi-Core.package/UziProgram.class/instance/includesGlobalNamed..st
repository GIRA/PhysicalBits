testing
includesGlobalNamed: globalName
	^ self allGlobals anySatisfy: [:g | g name = globalName]