writing
close
	self privateNextPut: self endMark.
	"Make sure it's not escaped"
	(eeprom read: position) = self endMark
		ifTrue: [eeprom write: position value: 0].
	"Make the instance unusable"
	position := nil
	