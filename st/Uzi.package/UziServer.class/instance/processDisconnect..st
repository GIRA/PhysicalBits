requests
processDisconnect: req
	protocol disconnect.
	req sendJSONResponse: protocol isConnected 