as yet unclassified
test3
	| eeprom reader |

	eeprom := EEPROM new.
	eeprom write: 15 value: begin.
	eeprom write: 30 value: end.
	eeprom write: 31 value: end.
	eeprom write: 35 value: end.
	
	reader := EEPROMReader on: eeprom.

	self assert: reader findOffset equals: 15.
	