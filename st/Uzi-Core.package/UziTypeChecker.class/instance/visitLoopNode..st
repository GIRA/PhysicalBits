visiting
visitLoopNode: node
	self assertBlock: node condition.
	self assertBlock: node body.
	
	node condition isBlock
		ifTrue: ["HACK(Richo):
			Loop conditions are special blocks that are allowed to end with an expression.
			That's why I need to handle it specially, otherwise it will be marked as invalid"
			node condition statements allButLast
				do: [:each | self assertStatement: each].
			self assertExpression: node condition statements last.
			self visitCollection: node condition statements].
	
	self visit: node body