tests
testConditionals
	| pp ast statements toArray |
	pp := parser.
	ast := pp parse: '#script1 ticking 1/s [
		3 > 4 ifTrue: [D13 on].
		3 < 4 ifFalse: [D13 off].
		4 > 3 ifTrue: [D13 on] ifFalse: [D13 off].
		4 < 3 ifFalse: [D13 on] ifTrue: [D13 off]]'.
	statements := ast scripts first body statements.
	self assert: 4 equals: statements size.
	toArray := [:send |
		{ send receiver value . send selector . (send arguments at: 1 ifAbsent: nil) value}].
	statements first ifNotNil: [:if |
		self assert: #(3 > 4) equals: (toArray value: if condition).
		self assert: if trueBranch isBlock.
		self assert: if falseBranch isNil.
		self assert: #(13 on nil) equals: (toArray value: if trueBranch statements first).]