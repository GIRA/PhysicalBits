initialize
sslConnect
	"Connect the client to a web server"

	| sqSSL |
	proxyServer ifNotNil:[ | resp |
		"If we have a proxy server, do the proxy connect"
		resp := self proxyConnect.
		resp isSuccess ifFalse:[^resp].
	].

	sqSSL := Smalltalk at: #SqueakSSL ifAbsent:[self error: 'SqueakSSL is missing'].
	"Convert the stream to a secure stream"
	stream := sqSSL secureSocketStream on: stream socket.
	stream timeout: timeout.
	"Do the SSL handshake"
	stream sslConnect.
	"And cert verification"
	stream verifyCert: self serverName.
	^nil "indicating success"
