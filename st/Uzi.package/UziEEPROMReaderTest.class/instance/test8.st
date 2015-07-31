as yet unclassified
test8
	| eeprom reader |

	eeprom := EEPROM new.
	eeprom write: 31 value: begin.
	eeprom write: 4 value: end.
	eeprom write: 5 value: begin.
	eeprom write: 15 value: end.
	
	
	reader := EEPROMReader on: eeprom.

	self assert: reader findOffset equals: 5.
	