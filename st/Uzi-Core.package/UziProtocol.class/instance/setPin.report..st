reporting
setPin: pinNumber report: report
	| msg |
	msg := self send: {
		self class msgOutSetReport. 
		pinNumber. 
		report ifTrue: [1] ifFalse: [0]
	}.
	report 
		ifTrue: [pinsReporting add: pinNumber]
		ifFalse: [pinsReporting remove: pinNumber ifAbsent: ["Do nothing"]].
	self triggerEvent: UziProtocolEvents reportingChange.
	^ msg