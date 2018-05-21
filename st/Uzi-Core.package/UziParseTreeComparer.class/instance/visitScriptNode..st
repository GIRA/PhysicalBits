visiting
visitScriptNode: node
	(nodesDict at: node) do: [:each | 
		self assert: node name
			equals: each name
			description: 'Script names don''t match'.
		self assert: node state
			equals: each state
			description: 'Script state doesn''t match'].
	self compareChildren: node