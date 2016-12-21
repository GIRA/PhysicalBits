sending
sendResponse: resp content: aString
	"Send a WebResponse with content."

	^self sendResponse: resp contentStream: aString readStream size: aString size