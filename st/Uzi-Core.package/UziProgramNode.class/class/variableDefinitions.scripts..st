instance creation
variableDefinitions: varsArray scripts: scriptsArray
	^ self basicNew 
		initializeWithVariableDefinitions: varsArray
		scripts: scriptsArray