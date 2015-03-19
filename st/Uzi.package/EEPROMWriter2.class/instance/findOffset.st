initializing
findOffset
	"The 0 position is always a pointer"
	| index val |
	index := 0.
	[val := eeprom read: index.
	val = 0 ifTrue: [
		"We found the offset"
		^ index].
	index := index + val]
		doWhileTrue: [index < eeprom size].
	"No offset, what do we do?"
	self flag: #Richo. "I should probably return an invalid value to show that nothing is written"
	^ -1