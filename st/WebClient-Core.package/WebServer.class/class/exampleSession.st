examples
exampleSession		"WebServer exampleSession"
	"This example uses a simple session cookie to avoid 
	authentication for every request."

	| server port realm |
	realm := 'Example Realm'.	"authentication realm"
	port := 9999.
	server := WebServer reset default.
	server listenOn: port.

	"Add a sample user (note: password is hashed, not stored verbatim)"
	server passwordAt: 'squeak' realm: realm put: 'squeak'.

	"Register the login service (sets cookie when successful)"
	server addService: '/login' action:[:req| 
		server  authenticate: req  realm: realm methods: #(digest)  do:[ | id |
			"Set the cookie and redirect back to /"
			server sessionAt: (id := UUID new hex) put: ''.
			req send302Response: (req fields at: 'url' ifAbsent:['/'])
				do:[:resp| resp setCookie: 'session' value: id path: '/']]].

	"Register the normal browse service"
	server addService: '/' action:[:req|
		"Check if we have a valid session cookie. If not redirect to login."
		(server sessionAt: (req cookieAt: 'session')) notNil
			ifTrue:[self browseRequest: req]
			ifFalse:[req send302Response: '/login?url=', req url encodeForHTTP]].
