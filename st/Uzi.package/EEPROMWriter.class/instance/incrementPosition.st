as yet unclassified
incrementPosition
	[position := position + 1 \\ eeprom size]
		doWhileTrue: [position isDivisibleBy: 8]