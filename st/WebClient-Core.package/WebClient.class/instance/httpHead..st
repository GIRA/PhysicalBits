methods
httpHead: urlString
	"Sends a HEAD request"

	^self httpHead: urlString do:[:req]
