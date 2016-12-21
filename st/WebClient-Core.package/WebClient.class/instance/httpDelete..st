methods
httpDelete: urlString
	"Sends an DELETE request"

	^self httpDelete: urlString do:[:req]
