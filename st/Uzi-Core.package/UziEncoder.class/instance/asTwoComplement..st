private
asTwoComplement: n
	^ n >= 0 
		ifTrue: n 
		ifFalse: [16rFF bitAnd: (n abs bitXor: 16rFF) + 1]