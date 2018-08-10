requests
processActivateReporting: req
	| pin |
	pin := req fields at: 'pin' ifAbsent: [nil].
	[pin ifNil: [self activateReportingForAll]
		ifNotNil: [self activateReportingFor: pin]] value.
		"on: Error
		do: [:err | ^ req send400Response: err description]."
	self sendJSONResponse: (self detail: req) for: req