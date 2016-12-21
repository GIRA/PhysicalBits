methods
httpHead: urlString
	"Sends a HEAD request"
	"WebClient httpHead: 'http://ftp.squeak.org/trunk'"

	^self httpDo:[:client| client httpHead: urlString].
