visiting
visitLoopNode: node
	self assertBlock: node condition.
	self assertBlock: node body.
	
	node condition isBlock
		ifTrue: ["HACK(Richo):
			Loop conditions are special blocks that are allowed to end in an expression.
			That's why I need to handle it specially, otherwise it will be marked as invalid"
			node condition statements
				withIndexDo: [:each :index |
					index = node condition statements size
						ifTrue: [self assertExpression: each]
						ifFalse: [self assertStatement: each]].
			self visitCollection: node condition statements].
	
	self visit: node body