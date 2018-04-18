accessing
locals
	^ self statements select: [:stmt | stmt isVariableDeclaration]