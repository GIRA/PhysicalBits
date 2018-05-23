visiting
visitAssignmentNode: node
	| name |
	self visit: node right.
	name := self varNameFor: node left.
	(script pop: name) node: node