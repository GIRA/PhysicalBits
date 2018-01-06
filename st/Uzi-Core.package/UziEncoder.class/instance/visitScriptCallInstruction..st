encoding instructions
visitScriptCallInstruction: instruction
	| index |
	index := (program scriptNames indexOf: instruction argument) - 1.
	index > 16rFF ifTrue: [self halt].
	index > 16rF 
		ifTrue: [stream nextPut: 16rFC; nextPut: index]
		ifFalse: [stream nextPut: (16rC0 bitOr: index)]