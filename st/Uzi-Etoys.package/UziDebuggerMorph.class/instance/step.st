stepping
step
	lastPC ~= debugger pc
		ifTrue: [lastPC := debugger pc.
			self changed: #stackFrames;
				changed: #sourceCodeSelection;
				changed: #textualFormSelection;
				changed: #locals;
				changed: #stackString;
				rebuild]