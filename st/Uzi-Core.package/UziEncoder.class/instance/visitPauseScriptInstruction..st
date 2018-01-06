encoding instructions
visitPauseScriptInstruction: instruction
	| index |
	index := (program scriptNames indexOf: instruction argument) - 1.
	index > 16r7F ifTrue: [self halt].
	index > 16r7 
		ifTrue: [stream nextPut: 16rFE; nextPut: (16r80 bitOr: index)]
		ifFalse: [stream nextPut: (16rE8 bitOr: index)]