methods
httpPost: urlString content: postData type: contentType
	"POST the data to the given url"

	^self httpPost: urlString content: postData type: contentType do:[:req]