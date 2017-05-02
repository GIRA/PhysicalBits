visiting
visitLoopNode: node
	| labelStart |
	labelStart := self newLabel.
	script label: labelStart.
	node evaluateBodyFirst 
		ifTrue: [self visit: node body].
		
	"We can't directly visit the condition because the loop condition
	is a special type of block which is allowed to leave a value on the
	stack. If we visit node as a block, the remaining value will be lost"
	node condition statements 
		do: [:stmt | self visit: stmt].
		
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