as yet unclassified
test4
	| eeprom reader |

	eeprom := EEPROM new.
	eeprom write: 15 value: begin.
	eeprom write: 30 value: begin.
	eeprom write: 31 value: begin.
	eeprom write: 35 value: end.
	
	reader := EEPROMReader4 on: eeprom.

	self assert: reader findOffset equals: 15.
	