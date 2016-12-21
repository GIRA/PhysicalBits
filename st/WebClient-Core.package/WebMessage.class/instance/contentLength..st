accessing
contentLength: aNumber
	"Sets the numeric value of the Content-Length header"

	^self headerAt: 'Content-Length' put: aNumber