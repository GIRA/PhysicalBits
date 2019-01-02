stepping
stepOverTrivialOn: anUziDebugger 
	^ self branchInstruction
		ifNil: [self stepOverRegularOn: anUziDebugger]
		ifNotNil: [:instr | { instr start }]