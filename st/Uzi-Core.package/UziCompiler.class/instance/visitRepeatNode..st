visiting
visitRepeatNode: node
	| labelEnd labelStart temp |
	"First we register a new temporary local variable"
	temp := script registerNewLocalWithValue: 0.
	labelEnd := self newLabel.
	labelStart := self newLabel.
	"Then we set temp = 0"
	(script push: 0) node: node.
	(script pop: temp name) node: node.
	"We compare temp with times"
	script label: labelStart.
	(script push: temp name) node: node.
	self visit: node times.
	(script prim: #lessThan) node: node.
	(script jz: labelEnd) node: node.
	"While temp is less than the expected times we execute the body"
	self visit: node body.
	"Before jumping back to the comparison, we increment temp"
	(script push: temp name) node: node.
	(script push: 1) node: node.
	(script prim: #add) node: node.
	(script pop: temp name) node: node.
	(script jmp: labelStart) node: node.
	script label: labelEnd