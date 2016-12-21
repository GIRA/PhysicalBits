examples
exampleAuth		"WebServer exampleAuth"
	"Simple example requiring basic auth"

	| server port realm |
	port := 9999.
	server := WebServer reset default.
	
	"The realm to use for the server"
	realm := 'Example Realm'.

	"Add a sample user (note: password is hashed, not stored verbatim)"
	server passwordAt: 'squeak' realm: realm put: 'squeak'.

	server listenOn: port.
	server addService: '/' action:[:req| 
		server 
			authenticate: req 
			realm: realm
			methods: #(digest basic) 
			do:[self browseRequest: req]].
