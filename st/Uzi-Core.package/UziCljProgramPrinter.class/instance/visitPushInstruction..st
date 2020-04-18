encoding instructions
visitPushInstruction: instruction
	| var |
	var := instruction argument.
	var name
		ifNil: [self write: '(emit/push-value {1})' format: { self variableValue: var}]
		ifNotNil: [self write: '(emit/read-global "{1}")' format: { var name }]