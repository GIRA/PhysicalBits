visiting
visitPinLiteralNode: node
	(nodesDict at: node) do: [:each |
		self assert: node type
			equals: each type
			description: 'Pin type doesn''t match'.
		self assert: node number
			equals: each number
			description: 'Pin number doesn''t match'].
	self compareChildren: node