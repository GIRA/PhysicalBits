methods
httpOptions: urlString
	"Sends an OPTIONS request"
	"WebClient httpOptions: 'http://ftp.squeak.org/*'"
	"WebClient httpOptions: 'http://ftp.squeak.org/4.1/Squeak4.1.zip'"

	^self httpDo:[:client| client httpOptions: urlString]
