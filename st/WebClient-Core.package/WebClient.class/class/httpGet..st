methods
httpGet: urlString
	"Get the response from the given url"
	"(WebClient httpGet: 'http://www.squeak.org') content"

	^self httpDo:[:client| client httpGet: urlString].