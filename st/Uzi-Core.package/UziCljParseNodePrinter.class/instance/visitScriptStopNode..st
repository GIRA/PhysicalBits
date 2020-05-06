visiting
visitScriptStopNode: node
	self write: '(ast/stop-node ['.
	node scripts
		do: [:each | self write: '"{1}"' format: { each }]
		separatedBy: [self write: ' '].
	self write: ']'