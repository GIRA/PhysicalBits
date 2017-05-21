initialization
initialize
	super initialize.
	stackFrameSelection := 1.
	stackFrame := debugger stackFrames 
		at: stackFrameSelection
		ifAbsent: [nil].
	printer := UziProgramPrinter new.
	self color: Color transparent;
		hResizing: #shrinkWrap;
		vResizing: #shrinkWrap;
		listDirection: #leftToRight;
		buildSubmorphs;
		break