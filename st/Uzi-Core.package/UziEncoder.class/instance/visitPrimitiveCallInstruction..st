encoding instructions
visitPrimitiveCallInstruction: instruction
	| arg |
	arg := instruction argument code.
	arg < 16 
		ifTrue: [stream nextPut: (16rA0 bitOr: arg)]
		ifFalse: [arg < 32
			ifTrue: [stream nextPut: (16rB0 bitOr: arg - 16)]
			ifFalse: [arg < 287
				ifTrue: [stream nextPut: 16rFA.
						stream nextPut: arg - 32]
				ifFalse: [self halt]]]