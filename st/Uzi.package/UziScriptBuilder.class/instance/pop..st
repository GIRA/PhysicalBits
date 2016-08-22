instruction creation
pop: arg
	| variable |
	variable := program registerVariable: arg.
	instructions add: (UziPopInstruction arg: variable) 