requests
processConnect: req
	| port |
	port := req fields at: 'port' ifAbsent: [^ req send400Response: 'Missing parameter: "port"'].
	[self controller connectOn: port]
		on: Error
		do: [:err | ^ req send400Response: err description].
	self sendJSONResponse: (self detail: req)
		for: req