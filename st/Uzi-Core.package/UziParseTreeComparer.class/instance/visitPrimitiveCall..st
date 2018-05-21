visiting
visitPrimitiveCall: node
	(nodesDict at: node) do: [:each | 
		self assert: node selector
			equals: each selector
			description: 'Primitive call selector doesn''t match'.
		self assert: node primitive
			equals: each primitive
			description: 'Primitive doesn''t match'].
	self compareChildren: node