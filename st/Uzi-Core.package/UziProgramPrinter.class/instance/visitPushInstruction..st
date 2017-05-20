encoding instructions
visitPushInstruction: instruction
	| arg |
	arg := instruction argument.	
	self indent;
		write: 'push: {1}' format: { (arg name ifNil: [arg value]) storeString }