encoding instructions
visitStopScriptInstruction: instruction
	| index |
	index := (program scriptNames indexOf: instruction argument) - 1.
	index > 16r7F ifTrue: [self halt].
	index > 16r7 
		ifTrue: [stream nextPut: 16rFE; nextPut: index]
		ifFalse: [stream nextPut: (16rE0 bitOr: index)]