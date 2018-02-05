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
		ifTrue: [	WorldState 
			addDeferredUIMessage: [Transcript 
				show: 'Free Arduino RAM: ';
				show: valueArduino;
				show: ' bytes';
				cr]].
	freeArduinoRAM := valueArduino.
	
	valueUzi := next value.
	freeUziRAM ~= valueUzi
		ifTrue: [	WorldState 
			addDeferredUIMessage: [Transcript 
				show: 'Free Uzi RAM: ';
				show: valueUzi;
				show: ' bytes';
				cr]].
	freeUziRAM := valueUzi.
	