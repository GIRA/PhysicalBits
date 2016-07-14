private
privateNextPut: byte
	"This method simply doesn't escape any char"
	[^ eeprom write: position value: byte]
		ensure: [self incrementPosition]