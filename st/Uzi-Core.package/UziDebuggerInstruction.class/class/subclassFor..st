instance creation
subclassFor: instructions
	(instructions size = 1 and: [instructions last isUnconditionalBranch])
		ifTrue: [^ UziDebuggerTrivialInstruction].
	instructions last isBranch ifTrue: [^ UziDebuggerBranchInstruction].
	instructions last isScriptCall ifTrue: [^ UziDebuggerCallInstruction].
	instructions last isReturn ifTrue: [^ UziDebuggerReturnInstruction].
	^ UziDebuggerRegularInstruction
	