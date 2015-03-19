accessing
nextPut: value
	[eeprom write: self actualIndex value: value]
			ensure: [self incrementPosition]