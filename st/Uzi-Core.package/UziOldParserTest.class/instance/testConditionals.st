tests
testConditionals
	| pp ast statements toArray |
	pp := parser.
	ast := pp parse: '@script1 ticking 1/s [
		3 > 4 ifTrue: [D13 on].
		3 < 4 ifFalse: [D13 off].
		4 > 3 ifTrue: [D13 on] ifFalse: [D13 off].
		4 < 3 ifFalse: [D13 on] ifTrue: [D13 off]]'.
	statements := ast scripts first body statements.
	
	toArray := [:send |
		{ send arguments first value . send selector . (send arguments at: 2 ifAbsent: nil) value}].
	
	self assert: 4 equals: statements size.
	statements first ifNotNil: [:if |
		self assert: #(3 > 4) equals: (toArray value: if condition).
		self assert: #(13 on nil) equals: (toArray value: if trueBranch statements first).
		self assert: if falseBranch statements isEmpty].
	statements second ifNotNil: [:if |
		self assert: #(3 < 4) equals: (toArray value: if condition).
		self assert: if trueBranch statements isEmpty.
		self assert: #(13 off nil) equals: (toArray value: if falseBranch statements first)].	
	statements third ifNotNil: [:if |
		self assert: #(4 > 3) equals: (toArray value: if condition).
		self assert: #(13 on nil) equals: (toArray value: if trueBranch statements first).
		self assert: #(13 off nil) equals: (toArray value: if falseBranch statements first)].
	statements fourth ifNotNil: [:if |
		self assert: #(4 < 3) equals: (toArray value: if condition).
		self assert: #(13 off nil) equals: (toArray value: if trueBranch statements first).
		self assert: #(13 on nil) equals: (toArray value: if falseBranch statements first)]