encoding instructions
visitScriptCallInstruction: instruction
	self indent;
		write: 'call: #{1}' format: { instruction argument }