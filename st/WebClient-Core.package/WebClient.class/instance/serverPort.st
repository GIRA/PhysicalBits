accessing
serverPort
	"Returns the port of the server:port description"

	^(server copyAfter: $:) 
		ifEmpty:[self defaultPort]
		ifNotEmpty:[:portString| portString asInteger].
