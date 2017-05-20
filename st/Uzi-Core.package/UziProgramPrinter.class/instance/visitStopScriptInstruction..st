encoding instructions
visitStopScriptInstruction: instruction
	self indent;
		write: 'stop: #{1}' format: { instruction argument }