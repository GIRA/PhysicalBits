bytecode creation
pushLit: arg
	| literal |
	literal := literals detect: [:lit | lit value = arg] ifNone: [nil].
	literal ifNil: [
		literal := UziLiteral value: arg.
		literals add: literal].
	bytecodes add: (UziPushLiteralBytecode arg: literal)