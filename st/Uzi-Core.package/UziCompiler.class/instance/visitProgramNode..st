visiting
visitProgramNode: node
	^ (Uzi program: [:p |
		program := p.
		self visitCollection: node imports.
		self visitCollection: node globals.
		self visitCollection: node scripts.
		self visitCollection: node primitives.
		program := nil]) node: node
		