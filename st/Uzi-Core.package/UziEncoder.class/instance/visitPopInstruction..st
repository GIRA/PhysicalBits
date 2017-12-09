encoding instructions
visitPopInstruction: instruction
	| arg |
	arg := self indexOfGlobal: instruction argument.
	arg > 16rF ifTrue: [self halt].
	stream nextPut: (16r90 bitOr: arg)
	