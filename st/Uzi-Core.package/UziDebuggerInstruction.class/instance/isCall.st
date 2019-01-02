testing
isCall
	^ self innerInstructions anySatisfy: [:instr | instr isScriptCall]