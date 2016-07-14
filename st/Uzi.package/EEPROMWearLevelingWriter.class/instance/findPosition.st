initialize-release
findPosition
	| read size wasEnd |
	size := eeprom size.
	read := [:i | eeprom read: i \\ size].
	"Look for the end mark on the entire memory"
	wasEnd := false.
	0 to: size - 1 do: [:i || current isEnd |
		current := read value: i.
		isEnd := current = self endMark.
		"If the previous byte was the end mark and the current is not
		we can safely say, we found our position"
		(wasEnd and: [isEnd not])
			ifTrue: [^ i - 1].
		"We only set the wasEnd flag if the current byte is the end mark
		and the previous was not. This way we ignore escaped bytes"
		wasEnd := wasEnd not and: [isEnd]].
	"We didn't find the mark. Start at the beginning"
	^ 0