connection
privateDisconnect
	"Private! Use #disconnect"
	self stopSteppingProcess.
	serial ifNotNil: #close.
	serial := portName := nil.
	self triggerEvent: #disconnect