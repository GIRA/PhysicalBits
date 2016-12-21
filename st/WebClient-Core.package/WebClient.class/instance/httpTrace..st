methods
httpTrace: urlString
	"Sends a TRACE request"

	^self httpTrace: urlString do:[:req]
