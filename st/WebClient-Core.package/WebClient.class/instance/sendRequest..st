sending
sendRequest: request
	"Send an http request"

	^self sendRequest: request content: nil size: 0