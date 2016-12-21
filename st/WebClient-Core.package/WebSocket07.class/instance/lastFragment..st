read/write
lastFragment: aStringOrBytes
	"Writes the last fragment of data.
	Note: Spec says that code is ZERO for final fragment."

	aStringOrBytes isString 
		ifTrue:[^self send: aStringOrBytes squeakToUtf8 code: 0 final: true]
		ifFalse:[^self send: aStringOrBytes code: 0 final: true].