writing
nextPut: byte
	[^ eeprom write: position value: byte]
		ensure: [self incrementPosition]