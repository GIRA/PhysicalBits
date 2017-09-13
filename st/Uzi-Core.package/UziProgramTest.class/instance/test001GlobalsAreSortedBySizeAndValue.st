tests
test001GlobalsAreSortedBySizeAndValue
	| program vars expected |
	vars := #(16777216 1099511627775 255 -1 
			16777215 65536 0.5 256 65535 0).
	program := Uzi program: [:p | p
		script: #blink
		ticking: true
		delay: 0
		instructions: [:s | vars do: [:v | s push: v]]].
	expected := #(0 16rFF 16r100 16rFFFF 16r10000
		16rFFFFFF -1 0.5 16r1000000 16rFFFFFFFFFF).
	self assert: expected equals: (program sortedGlobals collect: #value)