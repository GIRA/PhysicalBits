authentication
authAccept: method request: request realm: realm header: authHeader
	"Performs the requested authentication method. 
	Returns true if successfully authenticated.
	Subclasses can extend this method to support more auth methods."

	^method asLowercase caseOf: {
		['basic'] 	-> [self basicAuth: request realm: realm header: authHeader].
		['digest'] 	-> [self digestAuth: request realm: realm header: authHeader].
	} otherwise:[false].
