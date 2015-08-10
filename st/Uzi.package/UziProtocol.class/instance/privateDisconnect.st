connection
privateDisconnect
	"Private! Use #disconnect"
	self stopSteppingProcess;
		stopKeepAliveProcess.
	serial ifNotNil: #close.
	serial := portName := nil