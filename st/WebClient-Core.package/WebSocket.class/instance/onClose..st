running
onClose: aBlock
	"Set the handler block to invoke when the socket is closed"

	handlers at: 'close' put: aBlock.