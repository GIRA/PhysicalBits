json-decode
jsonTrueFrom: stream
	"Decodes 'true' from aStream"

	((stream next: 4) = 'true'
		and:[stream atEnd or:[stream peek isAlphaNumeric not]]) 
			 ifFalse:[^self error: 'Expected ''true'''].
	^true