visiting
visitLogicalOrNode: node
	| labelEnd labelTrue |
	node right hasSideEffects
		ifTrue: ["We need to short circuit"
			labelTrue := self newLabel.
			labelEnd := self newLabel.
			self visit: node left.
			(script jnz: labelTrue) node: node.
			self visit: node right.
			(script jmp: labelEnd) node: node.
			script label: labelTrue.
			(script push: 1) node: node.
			script label: labelEnd]
		ifFalse: ["Primitive call is enough"
			self visit: node left.
			self visit: node right.
			(script prim: #logicalOr) node: node]