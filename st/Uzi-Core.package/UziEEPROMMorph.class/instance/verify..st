stepping
verify: program
	| reader |
	reader := self readerClass on: eeprom.
	program do: [:byte | reader next = byte ifFalse: [self halt]]