bytecode creation
pop: arg
	| variable |
	variable := program registerVariable: arg.
	bytecodes add: (UziPopBytecode arg: variable) 