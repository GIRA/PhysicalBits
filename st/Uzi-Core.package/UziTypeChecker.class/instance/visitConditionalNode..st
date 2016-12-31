visiting
visitConditionalNode: node
	self assert: node condition isExpression
		description: 'Expression expected' 
		node: node condition.
	self assert: (node trueBranch isNil or: [node trueBranch isBlock])
		description: 'Block expected'
		node: node trueBranch.
	self assert: (node falseBranch isNil or: [node falseBranch isBlock])
		description: 'Block expected'
		node: node falseBranch.
	
	self visit: node condition.
	node trueBranch ifNotNil: [:branch | self visit: branch].
	node falseBranch ifNotNil: [:branch | self visit: branch].