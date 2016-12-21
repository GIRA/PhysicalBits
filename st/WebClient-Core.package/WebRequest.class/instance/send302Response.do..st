responses
send302Response: location do: aBlock
	"Send a 302 temporary redirect response"

	^self send3xxResponse: location code: 302 do: aBlock