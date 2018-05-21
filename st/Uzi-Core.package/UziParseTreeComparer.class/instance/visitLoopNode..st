visiting
visitLoopNode: node	
	(nodesDict at: node) do: [:each | 
		self assert: node negated 
			equals: each negated
			description: 'Negated flag doesn''t match'].
	self compareChildren: node