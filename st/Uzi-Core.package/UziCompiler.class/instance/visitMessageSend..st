visiting
visitMessageSend: node
	"Regular message send generates a primitive call"
	node receiver ifNotNil: [:rcvr | self visit: rcvr].
	self visitCollection: node arguments.
	script prim: node primitive name