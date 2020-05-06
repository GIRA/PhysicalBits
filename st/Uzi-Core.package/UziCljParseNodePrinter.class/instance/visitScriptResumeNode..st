visiting
visitScriptResumeNode: node
	self write: '(ast/resume-node ['.
	node scripts
		do: [:each | self write: '"{1}"' format: { each }]
		separatedBy: [self write: ' '].
	self write: '])'