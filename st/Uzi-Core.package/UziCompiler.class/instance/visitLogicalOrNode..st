visiting
visitLogicalOrNode: node
	| labelEnd labelTrue |
	labelTrue := self newLabel.
	labelEnd := self newLabel.
	self visit: node left.
	(script jnz: labelTrue) node: node.
	self visit: node right.
	(script jmp: labelEnd) node: node.
	script label: labelTrue.
	(script push: 1) node: node.
	script label: labelEnd