connection
privateDisconnect
	"Private! Use #disconnect"
	self stopSteppingProcess.
	serial ifNotNil: [:s | s close].
	serial := portName := nil.
	pinValues := Dictionary new.
	self triggerEvent: UziProtocolEvents disconnect