connection
privateDisconnect
	"Private! Use #disconnect"
	self stopSteppingProcess.
	serial ifNotNil: [:s | s close].
	serial := portName := nil.
	self triggerEvent: UziProtocolEvents disconnect