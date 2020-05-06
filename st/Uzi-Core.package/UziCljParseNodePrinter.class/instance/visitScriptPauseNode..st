visiting
visitScriptPauseNode: node
	self write: '(ast/pause-node ['.
	node scripts
		do: [:each | self write: '"{1}"' format: { each }]
		separatedBy: [self write: ' '].
	self write: ']'