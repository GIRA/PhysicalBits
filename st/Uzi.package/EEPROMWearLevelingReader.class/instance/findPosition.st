initialize-release
findPosition
	| size count reader position |
	size := eeprom size.
	reader := EEPROMReader on: eeprom.
	
	"Skip beginning end marks"
	count := 0.
	[count < size and: [reader peek = self endMark]]
		whileTrue: ["Skip this byte"
			reader next.
			count := count + 1].

	"Now, look for the end mark on the entire memory"
	count := 0.
	[count <= size] whileTrue: [
		count := count + 1.
		reader next = self endMark
			ifTrue: [reader peek = self endMark
				ifTrue: ["It was escaped. Skip next"
					reader next]
				ifFalse: ["We found it! Break out of the loop"
					count := size + 1]]].
	
	"Skip end mark and take note of its position"
	reader back.
	endPosition := reader position.
	
	"Finally, go back until you find the begin mark"
	position := count := 0.
	[count <= size] whileTrue: [
		count := count + 1.
		reader back = self beginMark
			ifTrue: [reader peekBack = self beginMark
				ifTrue: ["It was escaped. Skip back"
					reader back]
				ifFalse: ["We found it! Break out of the loop"
					position := reader position.
					count := size + 1]]].
		
	^ position