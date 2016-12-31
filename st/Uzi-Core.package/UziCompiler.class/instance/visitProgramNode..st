visiting
visitProgramNode: node
	^ Uzi program: [:p |
		program := p.
		self visitCollection: node variableDefinitions;
			visitCollection: node scripts.
		program := nil]