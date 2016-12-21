running
close
	"Request graceful close"

	stream isConnected ifTrue:[
		stream nextPut: (Character value: 255).
		stream nextPutAll: (Character value: 0).
		stream flush.
	].
