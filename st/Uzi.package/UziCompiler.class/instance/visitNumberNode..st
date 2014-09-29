visiting
visitNumberNode: node
	| bytecode |
	"First we register the literal if not registered already"
	(literals includes: node value)
		ifFalse: [literals add: node value].
	"Then we create the pushLit bytecode.
	The argument is the index on the literals array (zero based)"
	bytecode := UziPushLiteralBytecode arg: (literals indexOf: node value) - 1.
	"Finally, we register the bytecode"
	bytecodes add: bytecode
	