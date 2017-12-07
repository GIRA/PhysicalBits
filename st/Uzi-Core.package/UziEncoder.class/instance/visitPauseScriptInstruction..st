encoding instructions
visitPauseScriptInstruction: instruction
	| index |
	index := (program scriptNames indexOf: instruction argument) - 1.
	index > 16r7 ifTrue: [self halt].
	stream nextPut: (16rE8 bitOr: index)