accessing
stackFrameSelection: index
	stackFrameSelection := index.
	
	"Update"
	stackFrame := debugger stackFrames 
		at: stackFrameSelection
		ifAbsent: [nil].
	self changed