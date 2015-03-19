as yet unclassified
nextPut: value
	[eeprom write: position value: value]
		ensure: [self incrementPosition]