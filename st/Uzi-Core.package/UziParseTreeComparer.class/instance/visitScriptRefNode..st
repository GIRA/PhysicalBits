visiting
visitScriptRefNode: node
	(nodesDict at: node) do: [:each | 
		self assert: node scriptName
			equals: each scriptName
			description: 'Script names don''t match'].
	self compareChildren: node