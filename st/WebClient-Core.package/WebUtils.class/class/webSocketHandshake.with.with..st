websockets
webSocketHandshake: key1 with: key2 with: data
	"Do the actual WebSocket handshake computation"

	| bytes |
	bytes := ByteArray new: 16.
	bytes longAt: 1 put: key1 bigEndian: true.
	bytes longAt: 5 put: key2 bigEndian: true.
	bytes replaceFrom: 9 to: 16 with: data.
	^(self md5HashStream: bytes readStream) reversed