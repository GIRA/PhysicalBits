stepping
step
	lastPC ~= debugger pc
		ifTrue: [lastPC := debugger pc.
			self changed: #stackString;
				changed: #sourceCodeSelection;
				changed: #textualFormSelection;
				changed: #locals;
				rebuild]