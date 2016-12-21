responses
send302Response: location
	"Send a 302 temporary redirect response"

	^self send302Response: location do:[:resp].