instance creation
client: aWebClient request: aWebRequest response: aWebResponse
	"Creates a new exception"

	^(self new)
		client: aWebClient 
		request: aWebRequest 
		response: aWebResponse