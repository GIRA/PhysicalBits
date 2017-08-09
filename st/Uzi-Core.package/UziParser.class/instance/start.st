private
start
	^ (ws , variableDeclaration star , ws , script star, ws) token end
		==> [:token || vars scripts |
			vars := token parsedValue at: 2.
			scripts := token parsedValue at: 4.
			(UziProgramNode 
				variableDeclarations: vars
				scripts: scripts)
				token: token]