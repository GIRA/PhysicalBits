visiting
visitNumberNode: node
	| bytecode literal |
	"First we register the literal if not registered already"
	literal := literals detect: [:lit | lit value = node value] ifNone: [nil].
	literal ifNil: [
		literal := UziLiteral value: node value.
		literals add: literal].
	"Then we create the pushLit bytecode.
	The argument is the index on the literals array (zero based)"
	bytecode := UziPushLiteralBytecode arg: literal.
	"Finally, we register the bytecode"
	bytecodes add: bytecode
	