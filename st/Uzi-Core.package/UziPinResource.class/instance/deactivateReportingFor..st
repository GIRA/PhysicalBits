reporting
deactivateReportingFor: pinName
	| pinNumber |
	pinNumber := pinName asInteger.
	self protocol setPin: pinNumber report: false