responses
sendOptionsResponse: allowed
	"Send a 200 OK response for an OPTIONS request"

	^self send200Response: '' contentType: 'application/octet-stream' do:[:resp|
		resp headerAt: 'allow' put: (String streamContents:[:s|
			allowed do:[:m| s nextPutAll: m] separatedBy:[s nextPut: $,]
		]).
	].