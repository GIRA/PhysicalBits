encoding instructions
visitDupInstruction: instruction
	stream nextPut: 16rFF.
	self halt. "Argument is not encoded yet!"