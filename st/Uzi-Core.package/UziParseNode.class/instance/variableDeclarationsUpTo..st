accessing
variableDeclarationsUpTo: node
	^ (self children copyUpTo: node) select: [:each | each isVariableDeclaration]