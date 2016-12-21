initialize
on: aStream
	"Create a new WebSocket instance using the given SocketStream"

	stream := aStream.
	name := String streamContents:[:s| 
		stream socket remoteAddress 
			do:[:b| s print: b] 
			separatedBy:[s nextPut: $.]].
	name := name, ':', stream socket remotePort.
	