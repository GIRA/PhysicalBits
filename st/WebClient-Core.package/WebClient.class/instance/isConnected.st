testing
isConnected
	"Returns true if the client is still connected"

	^stream notNil and:[stream isConnected]