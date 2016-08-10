visiting
visitNumberNode: node
	| bytecode variable |
	"First we register the variable (if not registered already)"
	variable := self registerVariable: node.
	"Then we create the push bytecode.
	The argument is the index on the variables array (zero based)"
	bytecode := UziPushBytecode arg: variable.
	"Finally, we register the bytecode"
	bytecodes add: bytecode
	