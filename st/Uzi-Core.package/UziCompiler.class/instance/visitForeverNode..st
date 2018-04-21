visiting
visitForeverNode: node
	| labelStart |
	labelStart := self newLabel.
	script label: labelStart.
	self visit: node body.
	(script jmp: labelStart) node: node