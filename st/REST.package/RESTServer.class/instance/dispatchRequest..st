requests
dispatchRequest: request
	| requestPath |
	requestPath := request url subStrings: '/'.
	(self process: request path: requestPath)
		ifFalse: [request send404Response]