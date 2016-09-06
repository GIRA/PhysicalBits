encoding instructions
visitStopScriptInstruction: instruction
	| index |
	index := (program scriptNames indexOf: instruction argument) - 1.
	stream nextPut: (16rE0 bitOr: index)