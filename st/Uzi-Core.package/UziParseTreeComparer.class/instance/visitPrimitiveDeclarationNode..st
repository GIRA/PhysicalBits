visiting
visitPrimitiveDeclarationNode: node
	(nodesDict at: node) do: [:each |
		self assert: node alias
			equals: each alias
			description: 'Alias doesn''t match'.
		self assert: node name
			equals: each name
			description: 'Name doesn''t match'].
	self compareChildren: node