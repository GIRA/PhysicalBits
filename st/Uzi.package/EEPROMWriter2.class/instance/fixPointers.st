initializing
fixPointers
	"
	We did a full round trip, so now we need to fix the pointer chain.
	"
	offset := offset + 1 \\ eeprom size.
	offset > 254 ifTrue: ["Special case" self halt].
	"The first byte is always a pointer"
	eeprom write: 0 value: offset.
	"The next pointer is the offset itself"
	eeprom write: offset value: 0