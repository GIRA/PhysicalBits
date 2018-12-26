printing
printStackValueAt: i on: stream
	| curS curFP localIndex |
	curS := script.
	curFP := fp.
	[i > curFP] whileFalse: [| val |
		val := stack basicAt: curS variables size + 2 + curFP.
		curS := program scriptForPC: (val bitAnd: 16rFF).
		curFP := (val bitShift: -16) bitAnd: 16rFF.
		curFP = 255 ifTrue: [curFP := -1]].
	localIndex := i - curFP - 1.
	
	"Is it a variable?"
	localIndex < curS variables size
		ifTrue: [| var |
			var := curS variables at: localIndex + 1.
			stream nextPutAll: var displayName;
				nextPutAll: ': ';
				print: (stack at: i) maxDecimalPlaces: 3.
			^ self].
	
	"Is it the return value?"
	localIndex = curS variables size
		ifTrue: [stream nextPutAll: 'RV: ';
				print: (stack at: i) maxDecimalPlaces: 3. 
			^ self].
		
	"Is it FP/PC?"	
	localIndex = (curS variables size + 1)
		ifTrue: [| val |
			val := stack basicAt: i.
			stream nextPutAll: 'FP: ';
				print: ((val bitShift: -16) bitAnd: 16rFF);
				nextPutAll: ', ';
				nextPutAll: 'RA: ';
				print: (val bitAnd: 16rFF).				
			^ self].
		
	"It's just a temp value"
	stream print: (stack at: i) maxDecimalPlaces: 3.