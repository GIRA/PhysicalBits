accessing
instructionCount
	^ ((scripts collect: #bytecodes) collect: #size) sum
