cookies
setCookie: name value: value path: path expires: date domain: domain secure: aBool
	"Adds a Set-Cookie header"

	^self setCookie: ((WebCookie new)
			name: name;
			value: value;
			path: path;
			expiry: date;
			domain: domain;
			secure: aBool;
		yourself)