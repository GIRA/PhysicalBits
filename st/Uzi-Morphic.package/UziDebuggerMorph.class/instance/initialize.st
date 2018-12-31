initialization
initialize
	super initialize.
	stackFrameSelection := 1.
	stackFrame := debugger stackFrames 
		at: stackFrameSelection
		ifAbsent: [nil].
	printer := UziProgramPrinter new.
	self 
		hResizing: #shrinkWrap;
		vResizing: #shrinkWrap;
		listDirection: #leftToRight;
		color: self defaultBackgroundColor;
		buildSubmorphs.
	debugger
		when: #debuggerUpdate
		send: #update
		to: self