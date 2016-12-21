read/write
send: aStringOrBytes
	"Writes one frame worth of data"

	aStringOrBytes isString 
		ifTrue:[^self send: aStringOrBytes squeakToUtf8 code: 1]
		ifFalse:[^self send: aStringOrBytes code: 2].