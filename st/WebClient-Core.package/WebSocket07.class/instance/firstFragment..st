read/write
firstFragment: aStringOrBytes
	"Writes the first fragment of data"

	aStringOrBytes isString 
		ifTrue:[^self send: aStringOrBytes squeakToUtf8 code: 1 final: false]
		ifFalse:[^self send: aStringOrBytes code: 2 final: false].