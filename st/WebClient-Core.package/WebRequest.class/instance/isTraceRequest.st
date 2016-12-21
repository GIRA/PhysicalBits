testing
isTraceRequest
	"Returns true if this is an HTTP TRACE request"

	^self method = 'TRACE'