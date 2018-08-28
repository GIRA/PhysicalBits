visiting
visitProgramNode: node
	^ (Uzi program: [:p |
		program := p.
		self visitCollection: node globals.
		self visitCollection: node scripts.
		program := nil]) node: node
		