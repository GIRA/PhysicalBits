initialize-release
writeBeginMark
	| previousPos |
	"Make sure the begin mark is not escaped by checking the previous byte"
	previousPos := (position - 1) \\ eeprom size.
	(eeprom read: previousPos) = self beginMark 
		ifTrue: [eeprom write: previousPos value: 0].
	"Now we now we can safely write our begin mark"
	self privateNextPut: self beginMark 