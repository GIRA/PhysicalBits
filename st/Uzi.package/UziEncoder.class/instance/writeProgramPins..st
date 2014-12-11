encoding programs
writeProgramPins: pins
	"Three bytes in which each bit represents a pin.
	Pins included are marked with 1"
	| pinModes totalBytes pin |
	totalBytes := 3.
	pinModes := 0.
	pin := 0.
	1 to: totalBytes do: [:byte |
		7 to: 0 by: -1 do: [:shift |
			pinModes := pinModes bitOr: (((pins includes: pin) 
				ifTrue: [1] 
				ifFalse: [0]) << shift).
			pin := pin + 1].
		stream nextPut: pinModes.
		pinModes := 0]