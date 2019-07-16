initialization
initialize
	super initialize.
	stackFrameSelection := 1.
	stackFrame := debugger stackFrames 
		at: stackFrameSelection
		ifAbsent: [nil].
	printer := UziProgramPrinter new.
	stackFrames := #().
	sourceCode := stackString := locals := ''.

	self 
		hResizing: #shrinkWrap;
		vResizing: #shrinkWrap;
		listDirection: #leftToRight;
		color: self defaultBackgroundColor;
		buildSubmorphs.
	debugger protocol
		when: UziEvents debuggerUpdate
		send: #update
		to: self