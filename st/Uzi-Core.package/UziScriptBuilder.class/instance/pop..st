instruction creation
pop: arg
	| variable |
	variable := program registerVariable: arg.
	^ self addInstruction: (UziPopInstruction arg: variable) 