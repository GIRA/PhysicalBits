methods
httpTrace: urlString
	"Sends a TRACE request"
	"(WebClient httpTrace: 'http://lists.squeakfoundation.org') content"
	"(WebClient httpTrace: 'http://ftp.squeak.org/trunk') content"

	^self httpDo:[:client| client httpTrace: urlString]