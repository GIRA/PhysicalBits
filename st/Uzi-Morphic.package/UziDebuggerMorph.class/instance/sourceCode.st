accessing
sourceCode
	^ self currentInstruction ifNotNil: [:instr | instr sourceCode]