visiting
visitScriptCallNode: node
	(script call: node scriptName) node: node.
	node acceptReturnValue ifFalse: [(script prim: #pop) node: node]