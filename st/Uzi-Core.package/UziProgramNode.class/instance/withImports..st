copying
withImports: importsArray
	^ (self class 
		imports: importsArray
		globals: globals
		scripts: scripts
		primitives: primitives)
		token: token