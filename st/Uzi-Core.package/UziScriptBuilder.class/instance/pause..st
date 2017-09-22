instruction creation
pause: arg
	^ self addInstruction: (UziPauseScriptInstruction arg: arg)