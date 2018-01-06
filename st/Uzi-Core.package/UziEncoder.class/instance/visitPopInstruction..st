encoding instructions
visitPopInstruction: instruction
	| arg |
	arg := self indexOfGlobal: instruction argument.
	arg > 16rFF ifTrue: [self halt].
	arg > 16rF 
		ifTrue: [stream nextPut: 16rF9; nextPut: arg]
		ifFalse: [stream nextPut: (16r90 bitOr: arg)]
	