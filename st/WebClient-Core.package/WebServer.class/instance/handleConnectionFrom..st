handling
handleConnectionFrom: aSocket
	"Synchronously handle an incoming socket connection."

	| stream request |

	"Give the process a pretty name"
	Processor activeProcess name: (String streamContents:[:s|
		aSocket remoteAddress do:[:b| s print: b] separatedBy:[s nextPut: $.].
		s nextPut: $:; print: aSocket remotePort.
		s nextPutAll: ' - ', self class name, ' request handler'.
	]).

	"Read the request from the socket, and dispatch it.
	Since we've been forked already it's okay to block."
	[
		certName ifNil:["Regular request"
			stream := SocketStream on: aSocket.
		] ifNotNil:["SSL/TLS"
			| ssl |
			ssl := Smalltalk at: #SqueakSSL ifAbsent:[self error: 'SqueakSSL not installed'].
			stream := ssl secureSocketStream on: aSocket.
			[stream sslAccept: certName] on: Error do:[^stream destroy].
		].

		[aSocket isConnected] whileTrue:[

			"Read the next request from the connection. If the connection is closed,
			or if a network error occurs, or the read times out, just close the connection
			without further ado (rather than logging an error). Any other error resulting
			from parsing the input is treated as a 400 bad request."

			stream peek ifNil:[^aSocket destroy].
			request := self newRequest.
			[request readFrom: stream] on: Error do:[:ex| 
				(ex isKindOf: NetworkError) ifFalse:[
					[request send400Response] on: Error do:[:ignore].
				].
				^aSocket destroy].

			self dispatchRequest: request.
		].
	] on: Error do:[:ex|
		(ex isKindOf: NetworkError) ifFalse:[
			self log: 'Error in request handling: ', ex description.
			self log: (self errorReportFor: ex).
		].
	].