reporting
deactivateReportingForAll
	2 to: 19 do: [:pinNumber | 
		self protocol setPin: pinNumber report: false]