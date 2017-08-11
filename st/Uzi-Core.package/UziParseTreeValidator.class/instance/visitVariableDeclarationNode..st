visiting
visitVariableDeclarationNode: node
	self assertVariableDeclaration: node.
	node value ifNotNil: [:v | self assertExpression: v; visit: v]