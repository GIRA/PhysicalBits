visiting
visitRepeatNode: node
	| labelEnd labelStart local |
	local := 't', self newLabel.
	labelEnd := self newLabel.
	labelStart := self newLabel.
	script registerLocal: local value: 0.
	script label: labelStart.
	(script push: local) node: node.
	self visit: node times.
	(script prim: #lessThan) node: node.
	(script jz: labelEnd) node: node.
	self visit: node body.
	(script push: local) node: node.
	(script push: 1) node: node.
	(script prim: #add) node: node.
	(script pop: local) node: node.
	(script jmp: labelStart) node: node.
	script label: labelEnd