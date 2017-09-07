requests
processConnect: req
	| port |
	port := req fields at: 'port' ifAbsent: [^ req send400Response: 'Missing parameter: "port"'].
	[(port beginsWith: 'COM') 
		ifTrue: [protocol connectOnPort: port]
		ifFalse: [protocol connectOnSocket: port]]
		on: Error
		do: [:err | ^ req send400Response: err description].
	self sendJSONResponse: self detail
		for: req