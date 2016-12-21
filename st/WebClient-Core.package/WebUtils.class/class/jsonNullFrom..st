json-decode
jsonNullFrom: stream
	"Decodes 'null' from aStream"

	((stream next: 4) = 'null'
		and:[stream atEnd or:[stream peek isAlphaNumeric not]]) 
			ifFalse:[^self error: 'Expected ''null'''].
	^nil