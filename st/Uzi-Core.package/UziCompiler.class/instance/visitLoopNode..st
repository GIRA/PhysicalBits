visiting
visitLoopNode: node
	| labelStart |
	labelStart := self newLabel.
	script label: labelStart.
	node evaluateBodyFirst 
		ifTrue: [self visit: node body].
	self visit: node condition.
	(node body isEmpty or: [node evaluateBodyFirst])
		ifTrue: [
			node negated
				ifTrue: [(script jz: labelStart) node: node]
				ifFalse: [(script jnz: labelStart) node: node]]
		ifFalse: [| labelEnd |
			labelEnd := self newLabel.
			node negated
				ifTrue: [(script jnz: labelEnd) node: node]
				ifFalse: [(script jz: labelEnd) node: node].
			self visit: node body.
			(script jmp: labelStart) node: node.
			script label: labelEnd]