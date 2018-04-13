receiving
processFreeRAM
	| next valueArduino valueUzi |
	next := [| val1 val2 val3 val4 |
		val1 := serial next.
		val2 := serial next.
		val3 := serial next.
		val4 := serial next.
		val1 << 24 bitOr: (val2 << 16 bitOr: (val3 << 8 bitOr: val4))].
	
	valueArduino := next value.
	freeArduinoRAM ~= valueArduino
		ifTrue: [	UziTranscript 
			log: 'Free Arduino RAM: {1} bytes' 
			format: { valueArduino }].
	freeArduinoRAM := valueArduino.
	
	valueUzi := next value.
	freeUziRAM ~= valueUzi
		ifTrue: [	UziTranscript 
			log: 'Free Uzi RAM: {1} bytes' 
			format: { valueUzi }].
	freeUziRAM := valueUzi.
	