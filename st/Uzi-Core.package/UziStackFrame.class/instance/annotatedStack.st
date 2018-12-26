printing
annotatedStack
	stack ifNil: [^ ''].
	^ String streamContents: [:stream |		
		stack size to: 1 by: -1 do: [:i || float int |
			float := stack at: i.
			int := stack basicAt: i.
			
			"Index"
			stream nextPutAll: (i - 1) asTwoCharacterString; nextPutAll: ') '; tab.
			
			"HEX"
			(3 to: 0 by: -1) do: [:n || hex |
				hex := ((int bitShift: (8 * n * -1)) bitAnd: 16rFF) hex.
				hex := hex copyFrom: 4 to: hex size.
				hex size = 1 ifTrue: [hex := '0', hex].
				stream nextPutAll: hex; space].
			
			"Float"
			stream tab; nextPutAll: '|'; tab; 
				print: float maxDecimalPlaces: 3; 
				cr]]