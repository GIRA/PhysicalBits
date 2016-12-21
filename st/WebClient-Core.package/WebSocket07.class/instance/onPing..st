control requests
onPing: aBlock
	"Set the handler block to invoke when a ping is received"

	handlers at: 'ping' put: aBlock.