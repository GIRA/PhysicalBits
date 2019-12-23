private
asFormatString: aString
	"Hack to allow using the %1 syntax instead of {1}. 
	This method would simply transform the former to the latter"
	^ String streamContents: [:out || prev |
		prev := nil.
		aString do: [:char |
			prev = $%
				ifTrue: [char isDigit
					ifTrue: [out nextPut: ${; nextPut: char; nextPut: $}]
					ifFalse: [out nextPut: prev; nextPut: char]]
				ifFalse: [char ~= $%
					ifTrue: [out nextPut: char]].
			prev := char]]
	