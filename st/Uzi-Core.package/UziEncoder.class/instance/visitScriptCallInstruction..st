encoding instructions
visitScriptCallInstruction: instruction
	| index |
	index := (program scriptNames indexOf: instruction argument) - 1.
	index > 16rF ifTrue: [self halt].
	stream nextPut: (16rC0 bitOr: index)