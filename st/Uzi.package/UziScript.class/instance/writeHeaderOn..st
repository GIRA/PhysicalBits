as yet unclassified
writeHeaderOn: aStream
	| header |
	header := ((self isTicking ifTrue: [1] ifFalse: [0]) bitShift: 31) bitOr: self delay.
	3 to: 0 by: -1 do: [:i |
		aStream nextPut: ((header bitShift: -8 * i) bitAnd: 16rFF)]