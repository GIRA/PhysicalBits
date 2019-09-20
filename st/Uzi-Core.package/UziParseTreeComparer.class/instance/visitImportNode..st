visiting
visitImportNode: node
	(nodesDict at: node) do: [:each |
		self assert: node alias
			equals: each alias
			description: 'Import alias doesn''t match'.
		self assert: node path
			equals: each path
			description: 'Import path doesn''t match'.
		self assert: node isResolved
			equals: each isResolved
			description: 'Import resolved status doesn''t match'].
	self compareChildren: node