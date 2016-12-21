json-decode
jsonFalseFrom: stream
	"Decodes 'false' from aStream"

	((stream next: 5) = 'false' 
		and:[stream atEnd or:[stream peek isAlphaNumeric not]]) 
			ifFalse:[^self error: 'Expected ''false'''].
	^false