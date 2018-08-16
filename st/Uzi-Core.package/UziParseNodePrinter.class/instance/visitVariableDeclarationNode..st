visiting
visitVariableDeclarationNode: node
	self write: 'var {1}' format: { node name }.
	node value ifNotNil: [:val | self write: ' = '; visit: node value]