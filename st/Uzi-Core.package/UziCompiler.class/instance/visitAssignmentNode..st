visiting
visitAssignmentNode: node
	| name |
	self visit: node right.
	name := self localsInScope 
		detect: [:each | node left name = each name]
		ifFound: [:var | variableNames at: var]
		ifNone: [node left name].
	(script pop: name) node: node