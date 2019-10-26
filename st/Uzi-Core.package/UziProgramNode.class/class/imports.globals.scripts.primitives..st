instance creation
imports: importsArray globals: globalsArray scripts: scriptsArray primitives: primitivesArray
	^ self basicNew 
		initializeWithImports: importsArray
		globals: globalsArray
		scripts: scriptsArray
		primitives: primitivesArray