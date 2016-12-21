methods
httpGet: urlString do: aBlock
	"GET the response from the given url"
	"(WebClient httpGet: 'http://www.squeak.org') content"

	| request |
	self initializeFromUrl: urlString.
	request := self requestWithUrl: urlString.
	request method: 'GET'.
	userAgent ifNotNil:[request headerAt: 'User-Agent' put: userAgent].
	aBlock value: request.
	^self sendRequest: request
