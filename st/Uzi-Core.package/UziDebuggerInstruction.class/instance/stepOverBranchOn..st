stepping
stepOverBranchOn: debugger
	^ (self stepOverRegularOn: debugger),
		(self branchInstruction
			ifNil: [#()]
			ifNotNil: [:instr | instr isTrivial
				ifTrue: [instr stepOverOn: debugger]
				ifFalse: [instr interval]])