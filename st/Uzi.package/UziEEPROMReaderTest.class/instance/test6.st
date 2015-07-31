as yet unclassified
test6
	| eeprom reader |

	eeprom := EEPROM new.
	eeprom write: 31 value: begin.
	eeprom write: 35 value: begin.
	eeprom write: 36 value: begin.
	eeprom write: 121 value: end.
	eeprom write: 122 value: begin.
	eeprom write: 4 value: end.
	
	
	reader := EEPROMReader4 on: eeprom.

	self assert: reader findOffset equals: 122.
	