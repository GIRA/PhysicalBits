handling
invokeAction: action request: request
	"Invokes the given action. Subclasses can override this method to serialize
	actions if necessary. At this point, the request header has been read from
	the network, but not its content."

	^action valueWithArguments: {request}
