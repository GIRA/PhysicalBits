requests
processConnect: req
	| port |
	port := req fields at: 'port' ifAbsent: [^ req send400Response: 'Missing parameter: "port"'].
	[protocol connectOnPort: port]
		on: Error
		do: [:err | ^ req send400Response: err description].
	req sendJSONResponse: self detail