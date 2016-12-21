responses
send301Response: location
	"Send a 301 permanent redirect response"

	^self send301Response: location do:[:resp].