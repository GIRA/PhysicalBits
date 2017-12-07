encoding instructions
visitStartScriptInstruction: instruction
	| index |
	index := (program scriptNames indexOf: instruction argument) - 1.
	index > 16r7 ifTrue: [self halt].
	stream nextPut: (16rD0 bitOr: index)