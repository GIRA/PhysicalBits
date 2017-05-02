visiting
visitScriptCallNode: node
	node arguments do: [:each | self visit: each value].
	(script call: node scriptName) node: node