accessing
next
	[^ eeprom read: self actualIndex]
		ensure: [self incrementPosition]