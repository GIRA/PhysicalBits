sending
sendRequest: request content: contentStream size: streamSize
	"Send an http request"

	^self sendRequest: request contentBlock:[:aStream|
		contentStream ifNotNil:[
			"Upload content if provided"
			contentStream position: 0.
			request streamFrom: contentStream to: aStream size: streamSize 
				progress:[:total :amount|
					(HTTPProgress new) 
						total: total; 
						amount: amount;
						signal: 'Uploading...']]].
