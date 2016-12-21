responses
sendXmlResponse: xmlString
	"Send an a-ok 200 xml-ish response"

	^self send200Response: xmlString contentType: 'application/xml'