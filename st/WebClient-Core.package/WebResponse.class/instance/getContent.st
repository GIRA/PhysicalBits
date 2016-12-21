private
getContent
	"Do not read any content if this was a HEAD request"

	request method = 'HEAD' ifTrue:[^''].
	^super getContent