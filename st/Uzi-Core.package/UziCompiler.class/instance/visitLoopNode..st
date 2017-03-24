visiting
visitLoopNode: node
	| labelEnd labelStart |
	labelStart := self newLabel.
	labelEnd := self newLabel.
	script label: labelStart.
	self visit: node condition.
	(script jz: labelEnd) node: node.
	self visit: node body.
	(script jmp: labelStart) node: node.
	script label: labelEnd