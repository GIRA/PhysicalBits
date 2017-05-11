script creation
script: scriptName 
ticking: isTicking 
delay: delayMs 
args: argNames
locals: localNames
instructions: aBlock

	| builder |
	builder := UziScriptBuilder
		program: self
		scriptName: scriptName
		ticking: isTicking
		delay: delayMs
		argsNames: argNames
		localsNames: localNames.
	aBlock value: builder.
	^ scripts add: builder script