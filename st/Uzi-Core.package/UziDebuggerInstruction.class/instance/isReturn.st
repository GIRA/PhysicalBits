testing
isReturn
	^ self instructions anySatisfy: [:instr | instr isReturn]