requests
processToggleReporting: req
	| pin |
	pin := req fields at: 'pin' ifAbsent: [nil].
	[pin ifNil: [self toggleReportingForAll]
		ifNotNil: [self toggleReportingFor: pin]] value.
		"on: Error
		do: [:err | ^ req send400Response: err description]."
	self sendJSONResponse: self detail for: req