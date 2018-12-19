stepping
step
	debugger pc ifNil: [^ self].
	lastPC ~= debugger pc
		ifTrue: [lastPC := debugger pc.
			
			"Select the top frame"
			self stackFrameSelection: 1]