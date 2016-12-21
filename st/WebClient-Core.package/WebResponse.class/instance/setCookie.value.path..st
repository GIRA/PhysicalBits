cookies
setCookie: name value: value path: path 
	"Adds a Set-Cookie header"

	^self setCookie: ((WebCookie new)
			name: name;
			value: value;
			path: path;
		yourself)