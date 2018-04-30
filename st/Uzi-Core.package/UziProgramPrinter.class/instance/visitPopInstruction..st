encoding instructions
visitPopInstruction: instruction
	| arg |
	arg := instruction argument.
	self indent;
		saveInterval: [self write: 'pop: {1}' format: { 
			(arg name ifNil: [arg value] ifNotNil: [:n | n asSymbol]) storeString 
		}]