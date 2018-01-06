encoding instructions
visitStartScriptInstruction: instruction
	| index |
	index := (program scriptNames indexOf: instruction argument) - 1.
	index > 16r7F ifTrue: [self halt].
	index > 16r7 
		ifTrue: [stream nextPut: 16rFD; nextPut: index]
		ifFalse: [stream nextPut: (16rD0 bitOr: index)]