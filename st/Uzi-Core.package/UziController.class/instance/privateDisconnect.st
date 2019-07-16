connection
privateDisconnect
	"Private! Use #disconnect"
	self stopSteppingProcess.
	serial ifNotNil: [:s | s close].
	serial := portName := nil.
	pins := Dictionary new.
	debugger reset.
	self triggerEvent: UziEvents disconnect.
	Uzi logger error: 'Connection lost!'; flush