as yet unclassified
test1
	| eeprom reader |

	eeprom := EEPROM new.
	eeprom write: 15 value: begin.
	eeprom write: 30 value: end.
	
	reader := EEPROMReader4 on: eeprom.

	self assert: reader findOffset equals: 15.
	