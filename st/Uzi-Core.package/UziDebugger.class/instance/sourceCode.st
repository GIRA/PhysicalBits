accessing
sourceCode
	^ self currentInstruction
		ifNil: ''
		ifNotNil: [:instr | instr sourceCode]