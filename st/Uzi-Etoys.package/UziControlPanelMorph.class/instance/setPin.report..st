accessing
setPin: pinNumber report: report
	report
		ifTrue: [self pinsReporting add: pinNumber]
		ifFalse: [self pinsReporting remove: pinNumber ifAbsent: ["Do nothing"]].
	protocol setPin: pinNumber report: report.
	self rebuild