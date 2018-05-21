visiting
visitVariableDeclarationNode: node
	(nodesDict at: node) do: [:each | 
		self assert: node name
			equals: each name
			description: 'Variable name doesn''t match'].
	self compareChildren: node