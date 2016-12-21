sending
closeIfTransient: resp after: aBlock
	"Evaluate aBlock. Close the connection if it is transient.
	Inserts Connection: close header if needed."

	| close |
	close := self isPersistent not.
	close 
		ifTrue: [
			"Always be explicit and insert the Connection: close header"
			resp headerAt: 'Connection' put: 'close' ]
		ifFalse: [
			"We should tell the client that we keep this connection alive."
			resp headerAt: 'Connection' put: 'Keep-Alive' ].
	aBlock value.
	stream flush.
	close  ifTrue:[self close].
