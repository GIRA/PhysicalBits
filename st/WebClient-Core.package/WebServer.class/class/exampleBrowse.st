examples
exampleBrowse	"WebServer exampleBrowse"
	"This example implements a simple http server allowing to view 
	and download files (like browsing ftp sites etc)."

	| server port |
	port := 9999.
	server := WebServer reset default.
	server listenOn: port.
	server addService: '/' action:[:req| self browseRequest: req].
