converting
asWebSocket68
	"Convert this request into a WebSocket"

	| resp |
	resp := self newResponse protocol: 'HTTP/1.1' code: 101.
	resp status: 'HTTP/1.1 101 Web Socket Protocol Handshake'.

	"Chrome 4/5 is *VERY* sensitive to the order of the fields"
	resp headerAt: 'Upgrade' put: 'WebSocket'.
	resp headerAt: 'Connection' put: 'Upgrade'.

	resp headerAt: 'WebSocket-Origin' put: (self headerAt: 'Origin').
	resp headerAt: 'WebSocket-Location' put: ('ws://', (self headerAt: 'host'), self rawUrl).
	(self headerAt: 'WebSocket-Protocol' ifAbsent:[nil]) ifNotNil:[:hdr|
		resp headerAt: 'WebSocket-Protocol' put: hdr.
	].
	resp writeOn: stream.
	stream flush.
	^WebSocket00 on: stream.