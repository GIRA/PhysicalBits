encoding instructions
visitPushInstruction: instruction
	| arg |
	arg := (globals indexOf: instruction argument) - 1.
	arg > 16rF ifTrue: [self halt].
	stream nextPut: (16r80 bitOr: arg)