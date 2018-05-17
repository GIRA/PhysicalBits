accessing
variableDeclarationsUpTo: node
	^ (self children upTo: node) select: [:each | each isVariableDeclaration]