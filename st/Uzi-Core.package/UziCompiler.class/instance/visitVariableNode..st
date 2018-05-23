visiting
visitVariableNode: node
	| name |
	name := self localsInScope 
		detect: [:each | node name = each name]
		ifFound: [:var | variableNames at: var]
		ifNone: [node name].
	(script push: name) node: node