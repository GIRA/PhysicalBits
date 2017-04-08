script creation
script: scriptName ticking: isTicking delay: delayMs instructions: aBlock
	^ self script: scriptName 
		ticking: isTicking 
		delay: delayMs 
		args: #()
		instructions: aBlock