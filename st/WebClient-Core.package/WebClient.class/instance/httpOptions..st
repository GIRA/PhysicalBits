methods
httpOptions: urlString
	"Sends an OPTIONS request"

	^self httpOptions: urlString do:[:req]
