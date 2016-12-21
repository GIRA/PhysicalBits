accessing
contentType: aString
	"Sets the content type for the receiver"

	^self headerAt: 'Content-Type' put: aString