requests
processGlobalReport: req
	| globals report |
	globals := (req fields at: 'globals' ifAbsent: '') subStrings: ','.
	report := ((req fields at: 'report' ifAbsent: '') subStrings: ',') collect: [:each | each = 'true'].
	globals size = report size ifFalse: [^ req send400Response: 'Invalid request parameters'].
	[globals with: report do: [:globalName :reportEnabled |
		self controller setGlobal: globalName report: reportEnabled]]
			on: Error
			do: [:err | ^ req send400Response: err description].
	self send200ResponseFor: req