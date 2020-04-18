accessing
variableValue: var
	^ var value = Float infinity ifTrue: ['##Inf'] ifFalse: [var value]