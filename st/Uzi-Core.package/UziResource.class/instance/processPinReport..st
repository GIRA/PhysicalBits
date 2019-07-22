accessing-actions
processPinReport: req
	| pins report |
	pins := (req fields at: 'pins' ifAbsent: '') subStrings: ','.
	report := ((req fields at: 'report' ifAbsent: '') subStrings: ',') collect: [:each | each = 'true'].
	pins size = report size ifFalse: [^ req send400Response: 'Invalid request parameters'].
	[pins with: report do: [:pinName :reportEnabled |
		self controller setPin: pinName report: reportEnabled]]
			on: Error
			do: [:err | ^ req send400Response: err description].	
	self sendJSONResponse: (self detail: req) for: req