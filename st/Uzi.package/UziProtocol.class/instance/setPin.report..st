reporting
setPin: pinNumber report: report
	self send: {
		5. 
		pinNumber. 
		report ifTrue: [1] ifFalse: [0]
	}