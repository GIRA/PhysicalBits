script creation
script: scriptName ticking: isTicking delay: delayMs instructions: aBlock
	| builder delayVar |
	delayVar := self registerGlobal: delayMs.
	builder := UziScriptBuilder
		program: self
		scriptName: scriptName
		ticking: isTicking
		delay: delayVar.
	aBlock value: builder.
	^ scripts add: builder script