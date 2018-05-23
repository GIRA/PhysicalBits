visiting
visitVariableNode: node
	| name |
	name := self varNameFor: node.
	(script push: name) node: node