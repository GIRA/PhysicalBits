encoding instructions
visitPrimitiveCallInstruction: instruction
	| arg |
	arg := instruction argument code.
	arg < 16 
		ifTrue: [stream nextPut: (16rA0 bitOr: arg)]
		ifFalse: [stream nextPut: (16rB0 bitOr: arg - 16)]