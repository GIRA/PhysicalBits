connection
privateDisconnect
	"Private! Use #disconnect"
	self 
		fork: [self stopSteppingProcess.
			serial ifNotNil: [:s | s close].
			serial := portName := nil.
			self triggerEvent: #disconnect]
		named: 'Disconnecting...'