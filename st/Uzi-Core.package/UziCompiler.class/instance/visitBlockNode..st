visiting
visitBlockNode: node
	self visitCollection: node locals.
	node statements do: [:stmt |
		self visit: stmt.
		
		"If the statement was an expression, now we have a value on the stack 
		that we don't want. So we force a pop to throw away the value"
		stmt isExpression ifTrue: [(script prim: #pop) node: node]]