connection
privateDisconnect
	"Private! Use #disconnect"
	self stopSteppingProcess.
	serial ifNotNil: [:s | s close].
	serial := portName := nil.
	pins := Dictionary new.
	debugger reset.
	self triggerEvent: UziProtocolEvents disconnect