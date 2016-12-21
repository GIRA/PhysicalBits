converting
asWebSocket00
	"Convert this request into a WebSocket"

	| field key1 key2 bytes resp |
	resp := self newResponse protocol: 'HTTP/1.1' code: 101.
	resp status: 'HTTP/1.1 101 Web Socket Protocol Handshake'.

	"Chrome 4/5 is *VERY* sensitive to the order of the fields"
	resp headerAt: 'Upgrade' put: 'WebSocket'.
	resp headerAt: 'Connection' put: 'Upgrade'.

	field := self headerAt: 'Sec-WebSocket-Key1'.
	key1 := WebUtils extractWebSocketKey: field.
	field := self headerAt: 'Sec-WebSocket-Key2'.
	key2 := WebUtils extractWebSocketKey: field.
	bytes := WebUtils webSocketHandshake: key1 with: key2 with: (stream next: 8).

	resp headerAt: 'Sec-WebSocket-Origin' put: (self headerAt: 'Origin').
	resp headerAt: 'Sec-WebSocket-Location' put: ('ws://', (self headerAt: 'host'), self rawUrl).
	(self headerAt: 'Sec-WebSocket-Protocol' ifAbsent:[nil]) ifNotNil:[:hdr|
		resp headerAt: 'Sec-WebSocket-Protocol' put: hdr.
	].

	resp writeOn: stream.
	bytes ifNotNil:[stream nextPutAll: bytes].
	stream flush.
	^WebSocket00 on: stream.
