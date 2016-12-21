converting
asWebSocket07
	"Convert this request into a WebSocket"

	| resp hash nonce |
	resp := self newResponse protocol: 'HTTP/1.1' code: 101.
	resp status: 'HTTP/1.1 101 Web Socket Protocol Handshake'.

	"Chrome 4/5 is *VERY* sensitive to the order of the fields"
	resp headerAt: 'Upgrade' put: 'WebSocket'.
	resp headerAt: 'Connection' put: 'Upgrade'.

	nonce := self headerAt: 'Sec-WebSocket-Key'.
	hash := WebUtils webSocketHash07: nonce.

	resp headerAt: 'Sec-WebSocket-Accept' put: hash.
	(self headerAt: 'Sec-WebSocket-Origin' ifAbsent: [self headerAt: 'Origin']) 
		ifNotNil: [ :origin | resp headerAt: 'Sec-WebSocket-Origin' put: origin ].
	resp headerAt: 'Sec-WebSocket-Location' put: ('ws://', (self headerAt: 'host'), self rawUrl).
	(self headerAt: 'Sec-WebSocket-Protocol' ifAbsent:[nil]) ifNotNil:[:hdr|
		resp headerAt: 'Sec-WebSocket-Protocol' put: hdr.
	].

	resp writeOn: stream.
	stream flush.
	^(WebSocket07 on: stream)
		masking: false;
		yourself.
	"http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-07#section-4.1 says:
   ''Frames sent from the server to the client are not masked.''
	http://tools.ietf.org/html/rfc6455#section-5.1 says: 
   ''A client MUST close a connection if it detects a masked
   frame.''
	So not using masking is compatible with the 07 protocol and is required by RFC 6455."