accessing
contentWithProgress: progressBlock
	"Reads and caches available content and returns it."

	^content ifNil:[	| length |
		length := self contentLength.
		content := (stream isBinary ifTrue:[ByteArray] ifFalse:[ByteString]) 
			new: (length ifNil:[1000])
			streamContents:[:s| self streamFrom: stream to: s size: length progress: progressBlock]].