encoding instructions
visitStartScriptInstruction: instruction
	self indent;
		write: 'start: #{1}' format: { instruction argument }