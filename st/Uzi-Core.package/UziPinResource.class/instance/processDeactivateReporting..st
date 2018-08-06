requests
processDeactivateReporting: req
	| pin |
	pin := req fields at: 'pin' ifAbsent: [nil].
	[pin ifNil: [self deactivateReportingForAll]
		ifNotNil: [self deactivateReportingFor: pin]] value.
		"on: Error
		do: [:err | ^ req send400Response: err description]."
	self sendJSONResponse: self detail for: req