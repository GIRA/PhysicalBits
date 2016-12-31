instruction creation
push: arg
	| variable |
	variable := program registerVariable: arg.
	^ self addInstruction: (UziPushInstruction arg: variable)