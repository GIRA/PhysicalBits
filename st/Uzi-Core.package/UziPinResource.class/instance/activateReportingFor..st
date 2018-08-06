reporting
activateReportingFor: pinName
	| pinNumber |
	pinNumber := pinName asInteger.
	self protocol setPin: pinNumber report: true