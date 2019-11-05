visiting
visitNumberLiteralNode: node
	(nodesDict at: node) do: [:each | 
		self assert: node value
			equals: each value
			description: 'Number value doesn''t match'].
	self compareChildren: node