visiting
visitProgramNode: node
	^ (Uzi program: [:p |
		ast := node.
		program := p.
		self visitCollection: node variableDeclarations.
		self visitCollection: node scripts.
		ast := program := nil]) node: node
		