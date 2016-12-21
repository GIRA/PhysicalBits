listening
stopListener
	"Stops the server's listener process"

	listenerProcess ifNotNil:[listenerProcess terminate].
	listenerProcess := nil.