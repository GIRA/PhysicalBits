as yet unclassified
test7
	| eeprom reader |

	eeprom := EEPROM new.
	eeprom write: 31 value: begin.
	eeprom write: 4 value: end.
	
	
	reader := EEPROMReader on: eeprom.

	self assert: reader findOffset equals: 31.
	