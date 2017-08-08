tests
test033LogicalAndOperator
	| expr pp |
	pp := parser expression end.
	expr := pp parse: '0 && 1'.
	self assert: 0 equals: expr left value.
	self assert: 1 equals: expr right value.