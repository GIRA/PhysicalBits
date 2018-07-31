visiting
visitPrimitiveCall: node
	(nodesDict at: node) do: [:each | 
		self assert: node selector
			equals: each selector
			description: 'Primitive call selector doesn''t match'.
		self assert: node primitiveName
			equals: each primitiveName
			description: 'Primitive name doesn''t match'].
	self compareChildren: node