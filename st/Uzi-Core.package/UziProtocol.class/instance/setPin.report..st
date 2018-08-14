reporting
setPin: pinNumber report: report
	| msg |
	msg := self send: {
		self class msgOutSetReport. 
		pinNumber. 
		report ifTrue: [1] ifFalse: [0]
	}.
	self triggerEvent: UziProtocolEvents reportingChange.
	^ msg