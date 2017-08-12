instance creation
imports: importsArray variableDeclarations: globalsArray scripts: scriptsArray
	^ self basicNew 
		initializeWithImports: importsArray
		variableDeclarations: globalsArray
		scripts: scriptsArray