instruction creation
resume: arg
	^ self addInstruction: (UziResumeScriptInstruction arg: arg)