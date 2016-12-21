methods
httpPut: urlString content: postData type: contentType
	"Fire off an HTTP PUT request"

	^self httpDo:[:client| client httpPut: urlString content: postData type: contentType]