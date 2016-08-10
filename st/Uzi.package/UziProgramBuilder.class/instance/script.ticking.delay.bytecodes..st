script creation
script: scriptName ticking: isTicking delay: delayMs bytecodes: aBlock
	| builder |
	builder := UziScriptBuilder
		program: self
		scriptName: scriptName
		ticking: isTicking
		delay: delayMs.
	aBlock value: builder.
	scripts add: builder script