initialize-release
findPosition
	| size wasEnd count reader position |
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
	wasEnd := false.
	position := 0.
	[count <= size]
		whileTrue: [| next isEnd |
			next := reader peek.
			isEnd := next = self endMark.
			"If the previous byte was the end mark and the next is not
			we can safely say we found our position"
			(wasEnd and: [isEnd not])
				ifTrue: ["Set the position and break out of the loop"
					position := reader position - 1 \\ size.
					count := size + 1]
				ifFalse: ["We only set the wasEnd flag if the current byte is the end mark
						and the previous was not. This way we ignore escaped bytes"
					wasEnd := wasEnd not and: [isEnd].
					count := count + 1.
					reader next]].
	^ position