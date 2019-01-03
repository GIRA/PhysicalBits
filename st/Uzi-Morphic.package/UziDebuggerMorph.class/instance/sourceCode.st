accessing
sourceCode
	^ self currentInstruction
		ifNil: [sourceCode]
		ifNotNil: [:instr | sourceCode := instr sourceCode]