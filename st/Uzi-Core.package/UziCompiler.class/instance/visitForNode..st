visiting
visitForNode: node
	| labelEnd labelStart |
	"First, we need to declare the counter variable"
	self visit: node counterDeclaration.
	labelEnd := self newLabel.
	labelStart := self newLabel.
	"Then we initialize counter"
	self visit: node start.
	(script pop: node counter name) node: node.
	"We compare counter with top"
	script label: labelStart.
	(script push: node counter name) node: node.
	self visit: node stop.
	(script prim: #lessThanOrEquals) node: node.
	(script jz: labelEnd) node: node.
	"While counter is less than or equals stop we execute the body"
	self visit: node body.
	"Before jumping back to the comparison, we increment counter by step"
	(script push: node counter name) node: node.
	self visit: node step.
	(script prim: #add) node: node.
	(script pop: node counter name) node: node.
	(script jmp: labelStart) node: node.
	script label: labelEnd