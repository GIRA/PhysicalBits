encoding instructions
visitPushInstruction: instruction
	| arg |
	arg := instruction argument.	
	self indent;
		saveInterval: [self write: 'push: {1}' format: { 
			(arg name ifNil: [arg value] ifNotNil: [:n | n asSymbol]) storeString 
		}]