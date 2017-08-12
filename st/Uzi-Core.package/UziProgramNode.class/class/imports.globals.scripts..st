instance creation
imports: importsArray globals: globalsArray scripts: scriptsArray
	^ self basicNew 
		initializeWithImports: importsArray
		globals: globalsArray
		scripts: scriptsArray