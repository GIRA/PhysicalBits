accessing
locals
	^ self allChildren select: [:each | each isVariableDeclaration]