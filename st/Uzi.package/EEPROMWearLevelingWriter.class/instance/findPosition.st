initialize-release
findPosition
	| size wasEnd count reader |
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
	[count <= size]
		whileTrue: [| next isEnd |
			next := reader peek.
			isEnd := next = self endMark.
			"If the previous byte was the end mark and the next is not
			we can safely say we found our position"
			(wasEnd and: [isEnd not]) ifTrue: [^ reader position - 1 \\ size].
			"We only set the wasEnd flag if the current byte is the end mark
			and the previous was not. This way we ignore escaped bytes"
			wasEnd := wasEnd not and: [isEnd].
			count := count + 1.
			reader next].
		
	"We didn't find the mark. Start at the beginning"
	^ 0