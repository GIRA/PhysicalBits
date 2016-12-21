initialize
critical: aBlock
	"Evaluate aBlock protected byt the server's mutex.
	Necessary if code needs to manipulate internal server state."

	^mutex critical: aBlock
