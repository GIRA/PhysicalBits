initialize
newWebSocketOn: aStream
	"Create a new socket entitiy.
	Subclasses can override this method to provide a different response class"

	^WebSocket on: aStream