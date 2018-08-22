visiting
visitVariableDeclarationNode: node
	self saveIntervalFor: node during: [
		self write: 'var {1}' format: { node name }.
		node value ifNotNil: [:val | self write: ' = '; visit: node value]]