instance creation
imports: importsArray variableDefinitions: varsArray scripts: scriptsArray
	^ self basicNew 
		initializeWithImports: importsArray
		variableDefinitions: varsArray
		scripts: scriptsArray