encoding instructions
visitPopInstruction: instruction
	| arg |
	arg := instruction argument.
	self indent;
		write: 'pop: {1}' format: { (arg name ifNil: [arg value]) storeString }