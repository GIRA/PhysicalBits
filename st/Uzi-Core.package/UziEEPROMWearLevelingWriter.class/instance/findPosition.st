initialize-release
findPosition
	| size count reader2 position |
	size := eeprom size.
	reader2 := UziEEPROMReader on: eeprom.
	
	"Skip beginning end marks"
	count := 0.
	[count < size and: [reader2 peek = self endMark]]
		whileTrue: ["Skip this byte"
			reader2 next.
			count := count + 1].

	"Now, look for the end mark on the entire memory"
	position := count := 0.
	[count <= size] whileTrue: [
			count := count + 1.
			reader2 next = self endMark
				ifTrue: [reader2 peek = self endMark
					ifTrue: ["It was escaped. Skip next"
						reader2 next.
						count := count + 1]
					ifFalse: ["We found it! Break out of the loop"
						reader2 back.
						position := reader2 position.
						count := size + 1]]].
	
	^ position