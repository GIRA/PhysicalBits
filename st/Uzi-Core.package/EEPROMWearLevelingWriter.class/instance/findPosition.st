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
	position := count := 0.
	[count <= size] whileTrue: [
			count := count + 1.
			reader next = self endMark
				ifTrue: [reader peek = self endMark
					ifTrue: ["It was escaped. Skip next"
						reader next.
						count := count + 1]
					ifFalse: ["We found it! Break out of the loop"
						reader back.
						position := reader position.
						count := size + 1]]].
	
	^ position