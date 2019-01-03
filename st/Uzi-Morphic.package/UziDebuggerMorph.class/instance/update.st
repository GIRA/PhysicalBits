updating
update
	self stackFrameSelection: 1.
	self updateButtons.
	window ifNotNil: [:w | 
		w setLabel: ('Uzi Debugger @ {1}' format: { debugger scriptName })]