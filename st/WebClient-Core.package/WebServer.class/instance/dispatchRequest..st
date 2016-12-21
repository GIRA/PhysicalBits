handling
dispatchRequest: request
	"Look up the handler for a given request and dispatch to it"

	^self dispatchRequest: request url: request url