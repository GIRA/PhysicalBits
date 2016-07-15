writing
close
	writer nextPut: self endMark.
	"Make sure it's not escaped"
	(eeprom read: writer position) = self endMark
		ifTrue: [eeprom write: writer position value: 0].
	"Make the instance unusable"
	writer := nil
	