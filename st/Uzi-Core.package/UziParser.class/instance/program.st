grammar-program
program
	^ (ws , import star, ws, variableDeclaration star , ws , script star, ws) token end
		==> [:token || imports vars scripts |
			imports := token parsedValue at: 2.
			vars := token parsedValue at: 4.
			scripts := token parsedValue at: 6.
			(UziProgramNode 
				imports: imports
				variableDeclarations: vars
				scripts: scripts)
				token: token]