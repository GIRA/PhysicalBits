stepping
step
	lastPC ~= debugger pc
		ifTrue: [lastPC := debugger pc.
			self changed: #sourceCodeSelection;
				changed: #textualFormSelection;
				changed: #stackString;
				rebuild]