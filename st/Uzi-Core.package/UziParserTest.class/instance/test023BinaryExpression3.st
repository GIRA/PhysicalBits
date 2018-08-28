tests
test023BinaryExpression3
	| pp ast left right |
	pp := parser expression end.
	ast := pp parse: 'read(D13) * 4 + read(6)'.
	self assert: #+ equals: ast selector.
	left := ast arguments first value.
	self assert: #read equals: left arguments first value selector.
	self assert: #(13) 
		equals: (left arguments first value arguments 
			collect: [:each | each value value]).
	self assert: 4 equals: left arguments second value value.
	right := ast arguments second value.
	self assert: #read equals: right selector.
	self assert: #(6) 
		equals: (right arguments 
			collect: [:each | each value value])