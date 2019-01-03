accessing
stackFrames
	^ debugger stackFrames
		ifEmpty: [stackFrames]
		ifNotEmpty: [:sf | stackFrames := sf]