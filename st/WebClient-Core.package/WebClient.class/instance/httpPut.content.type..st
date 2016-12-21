methods
httpPut: urlString content: postData type: contentType
	"PUT the data to the given url"

	^self httpPut: urlString content: postData type: contentType do:[:req]