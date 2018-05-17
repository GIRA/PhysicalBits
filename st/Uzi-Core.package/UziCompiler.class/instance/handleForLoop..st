visiting
handleForLoop: node	
	| labelBegin labelEnd temp |	
	"First, we need to declare the counter variable"
	self visit: node counter.
	labelBegin := self newLabel.
	labelEnd := self newLabel.
	"Then we initialize counter"
	self visit: node start.
	(script pop: node counter name) node: node.
	"We declare a temp var to hold the step value for this iteration"	
	temp := script registerNewLocalWithValue: 0.
	"This is where the loop begins"
	script label: labelBegin.
	"Now we compare counter with stop. The comparison 
	can either be GTEQ or LTEQ depending on the sign of the 
	step (which is stored on temp)"
	(script push: node counter name) node: node.
	self visit: node stop.
	self visit: node step.
	(script pop: temp name) node: node.
	(script push: temp name) node: node.
	(script push: 0) node: node.
	(script jlte: 2) node: node.
	(script prim: #lessThanOrEquals) node: node.
	(script jmp: 1) node: node.
	(script prim: #greaterThanOrEquals) node: node.
	(script jz: labelEnd) node: node.
	"While counter doesn't reach the stop we execute the body"
	self visit: node body.
	"Before jumping back to the comparison, we increment counter by step"
	(script push: node counter name) node: node.
	(script push: temp name) node: node.
	(script prim: #add) node: node.
	(script pop: node counter name) node: node.
	(script jmp: labelBegin) node: node.
	script label: labelEnd