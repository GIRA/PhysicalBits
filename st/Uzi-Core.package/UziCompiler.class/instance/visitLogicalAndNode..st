visiting
visitLogicalAndNode: node
	| labelEnd labelFalse |
	labelFalse := self newLabel.
	labelEnd := self newLabel.
	self visit: node left.
	(script jz: labelFalse) node: node.
	self visit: node right.
	(script jmp: labelEnd) node: node.
	script label: labelFalse.
	(script push: 0) node: node.
	script label: labelEnd