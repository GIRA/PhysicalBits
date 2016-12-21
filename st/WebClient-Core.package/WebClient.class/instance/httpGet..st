methods
httpGet: urlString
	"GET the response from the given url"
	"(WebClient httpGet: 'http://www.squeak.org') content"

	^self httpGet: urlString do:[:req]
