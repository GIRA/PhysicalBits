running
onMessage: aBlock
	"Set the handler block to invoke when a message is received"

	handlers at: 'message' put: aBlock.
