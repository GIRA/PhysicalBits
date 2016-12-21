methods
httpDelete: urlString
	"Sends a DELETE request"
	"WebClient httpDelete: 'http://ftp.squeak.org/trunk'"

	^self httpDo:[:client| client httpDelete: urlString].
