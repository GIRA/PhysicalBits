visiting
handleIfFalse: node
	| label |
	self visit: node condition.
	label := self newLabel.
	(script jnz: label) node: node.
	"False block"
	self visit: node falseBranch.
	script label: label