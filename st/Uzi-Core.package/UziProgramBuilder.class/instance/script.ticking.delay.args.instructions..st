script creation
script: scriptName 
ticking: isTicking 
delay: delayMs 
args: argNames 
instructions: aBlock
	^ self script: scriptName 
		ticking: isTicking 
		delay: delayMs 
		args: argNames
		locals: #()
		instructions: aBlock