instance creation
variables: variables scripts: scriptsArray
	^ self basicNew 
		initializeWithVariables: variables asArray
		scripts: scriptsArray asArray