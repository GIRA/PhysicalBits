visiting
visitProgramNode: node
	^ (Uzi program: [:p |
		ast := node.
		program := p.
		node variableDefinitions do: [:def |
			program registerVariable: def name value: def value].
		self visitCollection: node scripts.
		ast := program := nil]) node: node
		