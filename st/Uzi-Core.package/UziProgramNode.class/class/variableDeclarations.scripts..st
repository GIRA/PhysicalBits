instance creation
variableDeclarations: varsArray scripts: scriptsArray
	^ self basicNew 
		initializeWithVariableDeclarations: varsArray
		scripts: scriptsArray