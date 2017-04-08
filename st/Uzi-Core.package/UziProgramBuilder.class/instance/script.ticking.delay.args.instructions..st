script creation
script: scriptName 
ticking: isTicking 
delay: delayMs 
args: argNames 
instructions: aBlock

	| builder |
	builder := UziScriptBuilder
		program: self
		scriptName: scriptName
		ticking: isTicking
		delay: delayMs
		argsNames: argNames.
	aBlock value: builder.
	^ scripts add: builder script