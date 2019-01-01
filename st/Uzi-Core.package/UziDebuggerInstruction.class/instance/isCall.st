testing
isCall
	^ self instructions anySatisfy: [:instr | instr isScriptCall]