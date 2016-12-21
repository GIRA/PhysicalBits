cookies
setCookie: aCookie
	"Adds a Set-Cookie header"

	^self addHeader: 'Set-Cookie' value: aCookie asString