encoding instructions
visitTurnOffInstruction: instruction
	self write: '(emit/turn-off-pin {1})' format: { instruction argument value  = Float infinity ifTrue: ['##Inf'] ifFalse: [instruction argument value]}