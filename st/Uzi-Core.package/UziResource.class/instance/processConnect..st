requests
processConnect: req
	| port |
	port := req fields at: 'port' ifAbsent: [^ req send400Response: 'Missing parameter: "port"'].
	[(port beginsWith: 'COM') 
		ifTrue: [self protocol connectOnPort: port]
		ifFalse: [self protocol connectOnSocket: port]]
		on: Error
		do: [:err | ^ req send400Response: err description].
	self sendJSONResponse: self detail
		for: req