instruction creation
push: arg
	| variable |
	variable := program registerVariable: arg.
	instructions add: (UziPushInstruction arg: variable) 