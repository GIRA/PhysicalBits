reporting
setPin: pinName report: report
	(self getPinNumber: pinName)
		ifNotNil: [:pinNumber || msg |
			report
				ifTrue: [self pinsReporting add: pinName]
				ifFalse: [self pinsReporting remove: pinName ifAbsent: ["Do nothing"]].
			msg := self send: {
				self class msgOutSetReport. 
				pinNumber. 
				report ifTrue: [1] ifFalse: [0]
			}.
			self triggerEvent: UziEvents reportingUpdate.
			^ msg]