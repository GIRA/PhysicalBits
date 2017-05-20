encoding instructions
visitReadInstruction: instruction	
	self indent;
		saveInterval: [self write: 'read: {1}' format: { instruction argument }]