visiting
visitBlockNode: node
	node statements do: [:stmt |
		self visit: stmt.
		
		"If the statement was a script call, now we have the return value on the
		stack but we don't want it. So we force a pop to throw away the value"
		stmt isScriptCall ifTrue: [(script prim: #pop) node: node]]