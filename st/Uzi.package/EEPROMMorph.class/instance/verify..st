stepping
verify: program
	| reader |
	reader := EEPROMWearLevelingReader on: eeprom.
	program do: [:byte | reader next = byte ifFalse: [self halt]]