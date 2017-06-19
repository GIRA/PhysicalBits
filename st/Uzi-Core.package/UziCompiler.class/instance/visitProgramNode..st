visiting
visitProgramNode: node
	^ (Uzi program: [:p |
		ast := node.
		program := p.
		node variableDeclarations do: [:def |
			program registerGlobal: def name value: def value].
		self visitCollection: node scripts.
		ast := program := nil]) node: node
		