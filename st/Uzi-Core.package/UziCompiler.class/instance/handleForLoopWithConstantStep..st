visiting
handleForLoopWithConstantStep: node
	| labelBegin labelEnd |
	"First, we need to declare the counter variable"
	self visit: node counter.
	labelBegin := self newLabel.
	labelEnd := self newLabel.
	"Then we initialize counter"
	self visit: node start.
	(script pop: node counter name) node: node.
	"We compare counter with top"
	script label: labelBegin.
	(script push: node counter name) node: node.
	self visit: node stop.
	"We can do this statically because we know the step is a number"
	node step value > 0
		ifTrue: [(script prim: #lessThanOrEquals) node: node]
		ifFalse: [(script prim: #greaterThanOrEquals) node: node].
	(script jz: labelEnd) node: node.
	"While counter is less than or equals stop we execute the body"
	self visit: node body.
	"Before jumping back to the comparison, we increment counter by step"
	(script push: node counter name) node: node.
	self visit: node step.
	(script prim: #add) node: node.
	(script pop: node counter name) node: node.
	(script jmp: labelBegin) node: node.
	script label: labelEnd