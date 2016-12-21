read/write
nextFragment: aStringOrBytes
	"Writes the first fragment of data
	Note: Spec says that code is ZERO for intermediate fragments."

	aStringOrBytes isString 
		ifTrue:[^self send: aStringOrBytes squeakToUtf8 code: 0 final: false]
		ifFalse:[^self send: aStringOrBytes code: 0 final: false].