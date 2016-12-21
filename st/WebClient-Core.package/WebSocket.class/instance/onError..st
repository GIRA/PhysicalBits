running
onError: aBlock
	"Set the handler block to invoke when an error occurs"

	handlers at: 'error' put: aBlock.