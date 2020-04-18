encoding instructions
visitReadInstruction: instruction
	self write: '(emit/read-pin {1})' format: { instruction argument value  = Float infinity ifTrue: ['##Inf'] ifFalse: [instruction argument value]}