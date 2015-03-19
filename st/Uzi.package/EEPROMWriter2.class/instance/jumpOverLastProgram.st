initializing
jumpOverLastProgram
	"
	This method will update the eeprom and the offset to start writing at the end of the
	last program.
	"
	| blockSize |
	offset = -1 ifTrue: [
		"No program was found. Just write 0 at the first byte"
		offset := 0.
		eeprom write: offset value: 0.
		^ self].
	"The byte next to the offset is the block size"	
	blockSize := eeprom read: offset + 1.
	"We change the current offset to point to the end of the block"
	eeprom write: offset value: blockSize + 2.
	"We update the offset"
	offset := offset + blockSize + 2.
	offset > (eeprom size - 2)
		ifTrue: ["It seems we did a full round trip"
			self fixPointers]
		ifFalse: [eeprom write: offset value: 0]