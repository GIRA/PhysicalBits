oauth
oAuthSign: webRequest url: requestUrl using: params
	"Sign the given webRequest using the set of oauth parameters provided"

	^self oAuthSign: webRequest url: requestUrl extra: #() using: params