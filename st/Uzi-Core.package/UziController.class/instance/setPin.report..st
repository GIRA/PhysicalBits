reporting
setPin: pinNumber report: report
	| msg |
	report
		ifTrue: [self pinsReporting add: pinNumber]
		ifFalse: [self pinsReporting remove: pinNumber ifAbsent: ["Do nothing"]].
	msg := self send: {
		self class msgOutSetReport. 
		pinNumber. 
		report ifTrue: [1] ifFalse: [0]
	}.
	self triggerEvent: UziEvents reportingChange.
	^ msg