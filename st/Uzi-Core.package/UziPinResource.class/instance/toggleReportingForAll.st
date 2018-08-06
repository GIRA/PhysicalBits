reporting
toggleReportingForAll
	2 to: 19 do: [:pinNumber | 
		self protocol setPin: pinNumber 
			report: (self protocol getPinReport: pinNumber) not]