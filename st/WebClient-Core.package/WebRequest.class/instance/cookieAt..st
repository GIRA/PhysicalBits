cookies
cookieAt: aString
	"Answer the value for a cookie with the given name"

	^self cookieAt: aString ifAbsent:['']