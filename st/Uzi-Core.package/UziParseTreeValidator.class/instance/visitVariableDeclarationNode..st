visiting
visitVariableDeclarationNode: node
	self assertVariableDeclaration: node.
	(self script notNil and: [(self script arguments includes: node) not])
		ifTrue: [self assert: (self localsInScope allSatisfy: [:each | node name ~= each name])
			description: 'Variable already declared'
			node: node].
	
	node value ifNotNil: [:v | self assertExpression: v; visit: v]