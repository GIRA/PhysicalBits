encoding instructions
visitReadInstruction: instruction	
	self indent;
		write: 'read: {1}' format: { instruction argument }