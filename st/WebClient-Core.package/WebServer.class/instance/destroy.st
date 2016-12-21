initialize
destroy
	"Destroys the receiver"

	self stopListener.
	self destroyConnections.
	listenerSocket ifNotNil:[listenerSocket destroy].

	"De-register the WebServer"
	(self class forUrl: self siteUrl ifAbsent:[self]) == self 
		ifTrue:[self class removeUrl: siteUrl]
