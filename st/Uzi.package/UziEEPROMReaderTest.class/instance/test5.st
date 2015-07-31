as yet unclassified
test5
	| eeprom reader |

	eeprom := EEPROM new.
	eeprom write: 15 value: begin.
	eeprom write: 30 value: end.
	eeprom write: 31 value: begin.
	eeprom write: 35 value: begin.
	eeprom write: 36 value: begin.
	eeprom write: 121 value: end.
	
	
	reader := EEPROMReader on: eeprom.

	self assert: reader findOffset equals: 31.
	