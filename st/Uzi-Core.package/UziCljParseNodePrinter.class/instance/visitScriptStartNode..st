visiting
visitScriptStartNode: node
	self write: '(ast/start-node ['.
	node scripts
		do: [:each | self write: '"{1}"' format: { each }]
		separatedBy: [self write: ' '].
	self write: ']'