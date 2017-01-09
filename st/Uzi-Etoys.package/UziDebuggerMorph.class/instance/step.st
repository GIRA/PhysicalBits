stepping
step
	lastPC ~= debugger pc
		ifTrue: [lastPC := debugger pc.
			self changed: #contentsSelection]