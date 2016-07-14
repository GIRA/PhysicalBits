stepping
useTheEEPROM
	| writer writeLimit size |
	started ifFalse: [^ self].
	writeLimit := eeprom writeLimit.
	writer := EEPROMWearLevelingWriter on: eeprom.
	size := (32 to: 128) atRandom.
	1 to: size do: [:n | 
		writer nextPut: n \\ 255.
		eeprom max >= writeLimit
			ifTrue: [started := false. ^ self]].
	writer close.
	programs := programs + 1.
	programsSize := programsSize + size