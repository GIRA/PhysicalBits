testing
isReturn
	^ self innerInstructions anySatisfy: [:instr | instr isReturn]