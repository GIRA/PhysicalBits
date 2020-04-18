encoding instructions
visitWriteInstruction: instruction
	self write: '(emit/write-pin {1})' format: { instruction argument value  = Float infinity ifTrue: ['##Inf'] ifFalse: [instruction argument value]}