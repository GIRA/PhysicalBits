bytecode creation
push: arg
	| variable |
	variable := program registerVariable: arg.
	bytecodes add: (UziPushBytecode arg: variable) 