grammar-program
program
	^ (ws , import star, ws, variableDeclaration star , ws , (primitive / script) star, ws) token end
		==> [:token || imports globals scripts primitives |
			imports := token parsedValue at: 2.
			globals := token parsedValue at: 4.
			scripts := (token parsedValue at: 6) reject: [:each | each isPrimitiveDeclaration].
			primitives := (token parsedValue at: 6) select: [:each | each isPrimitiveDeclaration].
			(UziProgramNode 
				imports: imports
				globals: globals
				scripts: scripts
				primitives: primitives)
				token: token]