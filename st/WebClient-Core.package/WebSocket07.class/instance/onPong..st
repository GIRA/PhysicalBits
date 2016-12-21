control requests
onPong: aBlock
	"Set the handler block to invoke when a pong is received"

	handlers at: 'pong' put: aBlock.