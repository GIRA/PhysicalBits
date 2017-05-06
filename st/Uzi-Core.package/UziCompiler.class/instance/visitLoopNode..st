visiting
visitLoopNode: node
	| labelStart |
	labelStart := self newLabel.
	script label: labelStart.
	self visit: node pre.
	self visit: node condition.
	node post isEmpty
		ifTrue: [
			node negated
				ifTrue: [(script jz: labelStart) node: node]
				ifFalse: [(script jnz: labelStart) node: node]]
		ifFalse: [| labelEnd |
			labelEnd := self newLabel.
			node negated
				ifTrue: [(script jnz: labelEnd) node: node]
				ifFalse: [(script jz: labelEnd) node: node].
			self visit: node post.
			(script jmp: labelStart) node: node.
			script label: labelEnd]