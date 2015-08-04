requests
processConnect: req
	| port |
	port := req fields at: 'port' ifAbsent: [^ req send400Response: 'Missing parameter: "port"'].
	protocol connectOnPort: port.
	req sendJSONResponse: protocol isConnected 