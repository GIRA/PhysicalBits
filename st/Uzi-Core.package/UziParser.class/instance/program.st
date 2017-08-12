grammar-program
program
	^ (ws , import star, ws, variableDeclaration star , ws , script star, ws) token end
		==> [:token || imports globals scripts |
			imports := token parsedValue at: 2.
			globals := token parsedValue at: 4.
			scripts := token parsedValue at: 6.
			(UziProgramNode 
				imports: imports
				globals: globals
				scripts: scripts)
				token: token]