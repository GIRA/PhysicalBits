tests
test023BinaryExpression3
	| pp ast left right |
	pp := parser expression end.
	ast := pp parse: 'read(D13) * 4 + read(6)'.
	self assert: #+ equals: ast selector.
	left := ast arguments first.
	self assert: #read equals: left arguments first selector.
	self assert: #(13) equals: (left arguments first arguments collect: [:each | each value]).
	self assert: 4 equals: left arguments second value.
	right := ast arguments second.
	self assert: #read equals: right selector.
	self assert: #(6) equals: (right arguments collect: [:each | each value])