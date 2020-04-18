encoding instructions
visitTurnOnInstruction: instruction
	self write: '(emit/turn-on-pin {1})' format: { instruction argument value  = Float infinity ifTrue: ['##Inf'] ifFalse: [instruction argument value]}