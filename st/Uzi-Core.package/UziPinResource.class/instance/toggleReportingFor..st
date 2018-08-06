reporting
toggleReportingFor: pinName
	| pinNumber |
	pinNumber := pinName asInteger.
	self protocol setPin: pinNumber 
		report: (self protocol getPinReport: pinNumber) not