visiting
visitVariableDeclarationNode: node
	self assertVariableDeclaration: node.
	(self script notNil and: [self path second ~= self script])
		ifTrue: [	self assert: (self localsInScope includes: node) not
			description: 'Variable already declared'
			node: node].
	
	node value ifNotNil: [:v | self assertExpression: v; visit: v]