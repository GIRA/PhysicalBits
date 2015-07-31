as yet unclassified
test9
	| eeprom reader |

	eeprom := EEPROM new.
	eeprom write: 1 value: begin.
	eeprom write: 2 value: begin.
	eeprom write: 3 value: begin.
	eeprom write: 10 value: end.	
	
	reader := EEPROMReader on: eeprom.

	self assert: reader findOffset equals: 5.
	