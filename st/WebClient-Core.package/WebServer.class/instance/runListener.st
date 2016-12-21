listening
runListener
	"Runs the server's listener loop. 
	Accepts incoming connections and handles them asynchronously"

	| connectionSocket |
	[[[listenerSocket notNil and:[listenerSocket isValid]] whileTrue:[
		"Waiting for five seconds means that WebServer will be
		back up after roughly five seconds when the system restarts."
		connectionSocket := listenerSocket waitForAcceptFor: 5.
		"Fork this off since or else errors in acceptClient: kill the listener"
		connectionSocket ifNotNil:[self asyncHandleConnectionFrom: connectionSocket].
	]] ensure:[
		"Logging here is only for the purpose of catching sockets going randomly bad"
		self log: 'listener terminating, socket: ', (listenerSocket ifNotNil:[:s| s statusString]).

		"Try to restart the listener"
		(listenerSocket notNil and:[listenerSocket isValid not]) ifTrue:[
			listenerSocket destroy.
			listenerProcess := nil.
			"Wait a little before retrying"
			(Delay forSeconds: 2) wait.
			^self listenOn: port interface: interface
		].
	]] on: Error do:[:ex| self log: ex].
