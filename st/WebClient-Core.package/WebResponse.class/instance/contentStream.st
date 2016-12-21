accessing
contentStream
	"Answers a stream for the conent."

	(self headerAt: 'transfer-encoding') = 'chunked' ifTrue:[
		^WebChunkedStream on: stream.
	].
	^super contentStream