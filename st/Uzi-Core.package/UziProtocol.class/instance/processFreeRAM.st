receiving
processFreeRAM
	| val1 val2 val3 val4 value |
	val1 := serial next.
	val2 := serial next.
	val3 := serial next.
	val4 := serial next.
	value := (val1 << 24 bitOr: (val2 << 16 bitOr: (val3 << 8 bitOr: val4))).
	freeRAM ~= value
		ifTrue: [	WorldState 
			addDeferredUIMessage: [Transcript 
				show: 'Free RAM: ';
				show: value;
				show: ' bytes';
				cr]].
	freeRAM := value