script building
script: scriptName ticking: isTicking delay: delayMs bytecodes: aBlock
	| builder |
	builder := self new.
	aBlock value: builder.
	^ UziScript
		name: scriptName
		ticking: isTicking
		delay: delayMs
		literals: builder literals
		variables: builder variables
		bytecodes: builder bytecodes