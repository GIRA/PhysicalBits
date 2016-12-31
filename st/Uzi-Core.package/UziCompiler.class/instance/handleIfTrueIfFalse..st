visiting
handleIfTrueIfFalse: node
	| labelEnd labelFalse |
	self visit: node condition.
	labelFalse := self newLabel.
	labelEnd := self newLabel.
	script jz: labelFalse.
	"True block"
	self visit: node trueBranch.
	script jmp: labelEnd.
	script label: labelFalse.
	"False block"
	self visit: node falseBranch.
	script label: labelEnd