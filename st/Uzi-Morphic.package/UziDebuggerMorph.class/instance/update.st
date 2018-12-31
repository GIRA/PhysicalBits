updating
update
	self stackFrameSelection: 1.
	self unlockButtons.
	window setLabel: ('Uzi Debugger @ {1}' format: { debugger scriptName })