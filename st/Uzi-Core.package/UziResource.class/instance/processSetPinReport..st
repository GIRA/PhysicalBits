accessing-actions
processSetPinReport: req
	| pinNumber reportEnabled |
	pinNumber := (req fields at: 'pinNumber' ifAbsent: [nil]) asInteger
		ifNil: [^ req send400Response: 'Missing parameter: "pinNumber"'].
	reportEnabled := (req fields at: 'reportEnabled' ifAbsent: [false]) = 'true'.
	[self controller setPin: pinNumber asInteger report: reportEnabled]
		on: Error
		do: [:err | ^ req send400Response: err description].	
	self sendJSONResponse: (self detail: req)
		for: req