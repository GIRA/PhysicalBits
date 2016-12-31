visiting
handleIfTrueIfFalse: node
	| labelEnd labelFalse |
	self visit: node condition.
	labelFalse := self newLabel.
	labelEnd := self newLabel.
	(script jz: labelFalse) node: node.
	"True block"
	self visit: node trueBranch.
	(script jmp: labelEnd) node: node.
	script label: labelFalse.
	"False block"
	self visit: node falseBranch.
	script label: labelEnd