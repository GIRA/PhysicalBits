visiting
visitLogicalAndNode: node
	| labelEnd labelFalse |
	node right hasSideEffects
		ifTrue: ["We need to short-circuit"
			labelFalse := self newLabel.
			labelEnd := self newLabel.
			self visit: node left.
			(script jz: labelFalse) node: node.
			self visit: node right.
			(script jmp: labelEnd) node: node.
			script label: labelFalse.
			(script push: 0) node: node.
			script label: labelEnd]
		ifFalse: ["Primitive call is enough"
			self visit: node left.
			self visit: node right.
			(script prim: #logicalAnd) node: node]