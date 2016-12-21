examples
exampleSSL: certName

	"This example implements a simple https server allowing to view 
	and download files (like browsing ftp sites etc):

		WebServer exampleSSL: 'Internet Widgits Pty'.
		WebServer exampleSSL: '/home/andreas/certs/testcert.pem'.

	"

	| server port |
	port := 8443.
	server := WebServer reset default.
	server accessLog: Transcript.
	server certName: certName.
	server listenOn: port.
	server addService: '/' action:[:req| self browseRequest: req].
