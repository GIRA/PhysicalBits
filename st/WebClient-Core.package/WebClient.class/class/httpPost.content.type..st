methods
httpPost: urlString content: postData type: contentType
	"Fire off an HTTP post request"

	^self httpDo:[:client| client httpPost: urlString content: postData type: contentType]
