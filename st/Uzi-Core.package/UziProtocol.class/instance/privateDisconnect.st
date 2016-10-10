connection
privateDisconnect
	"Private! Use #disconnect"
	self stopSteppingProcess.
	serial ifNotNil: [serial close].
	serial := portName := nil.
	self triggerEvent: #disconnect