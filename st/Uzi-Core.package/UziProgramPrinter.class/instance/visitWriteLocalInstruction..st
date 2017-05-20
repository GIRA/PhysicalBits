encoding instructions
visitWriteLocalInstruction: instruction
	self indent;
		write: 'pop: #{1}' format: { instruction argument }