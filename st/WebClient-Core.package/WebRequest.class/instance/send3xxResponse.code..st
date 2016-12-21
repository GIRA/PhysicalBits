responses
send3xxResponse: location code: statusCode
	"Send a 3xx redirect response"

	^self send3xxResponse: location code: statusCode do:[:resp].