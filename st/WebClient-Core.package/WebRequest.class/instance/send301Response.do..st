responses
send301Response: location do: aBlock
	"Send a 301 permanent redirect response"

	^self send3xxResponse: location code: 301 do: aBlock