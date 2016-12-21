testing
isHeadRequest
	"Returns true if this is an HTTP HEAD request"

	^self method = 'HEAD'