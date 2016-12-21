testing
isOptionsRequest
	"Returns true if this is an HTTP OPTIONS request"

	^self method = 'OPTIONS'