encoding scripts
writeScriptHeader: anUziScript
	| header |
	"First byte:
		1 bit : isTicking (1 true / 0 false)
		1 bit: hasDelay (1 true / 0 false)
		1 bit: hasArguments (1 true / 0 false)
		1 bit: hasLocals (1 true / 0 false)
		4 bits: reserved for future use"	
	header := 0.
	header := header bitOr: ((anUziScript isTicking ifTrue: [1] ifFalse: [0]) bitShift: 7).
	header := header bitOr: ((anUziScript hasDelay ifTrue: [1] ifFalse: [0]) bitShift: 6).
	header := header bitOr: ((anUziScript hasArguments ifTrue: [1] ifFalse: [0]) bitShift: 5).
	header := header bitOr: ((anUziScript hasLocals ifTrue: [1] ifFalse: [0]) bitShift: 4).
	stream nextPut: (header bitAnd: 16rFF).
	"If the script has a delay write its index on the global list"
	anUziScript hasDelay 
		ifTrue: [stream nextPut: (self indexOfGlobal: anUziScript delay)].
	"If the script has arguments write the argument count"
	anUziScript hasArguments 
		ifTrue: [stream nextPut: anUziScript arguments size].
	"If the script has locals write the local count followed by each local index on the global list"
	anUziScript hasLocals
		ifTrue: [stream nextPut: anUziScript locals size.
			anUziScript locals do: [:local || var |
				var := globals detect: [:g | g isConstant and: [g value = local value]].
				stream nextPut: (self indexOfGlobal: var)]]
	