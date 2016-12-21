authentication
authHeader: method request: request realm: realm
	"Answer the authentication header for the given method.
	Subclasses can extend this method to support more auth methods."

	^method asLowercase caseOf: {
		['basic'] 	-> ['Basic realm="', realm,'"'].
		['digest'] 	-> ['Digest realm="', realm,'", nonce="', self newNonce,'", qop="auth"'].
	} otherwise:[nil].
	
	