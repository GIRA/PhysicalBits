stepping
write: program
	| writer |
	writer := EEPROMWearLevelingWriter on: eeprom.
	program do: [:byte |
		writer nextPut: byte.
		eeprom max >= eeprom writeLimit
			ifTrue: [^ false]].
	writer close.
	^ true