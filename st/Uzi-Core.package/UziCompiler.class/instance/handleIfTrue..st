visiting
handleIfTrue: node
	| label |
	self visit: node condition.
	label := self newLabel.
	script jz: label.
	"True block"
	self visit: node trueBranch.
	script label: label