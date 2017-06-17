tests
test011CallWithRecursiveArgument
	| pp ast arg2 |
	pp := parser call end.
	ast := pp parse: 'write(D11, read(A1))'.
	self assert: #write equals: ast selector.
	self assert: 2 equals: ast arguments size.
	self assert: 11 equals: ast arguments first value.
	arg2 := ast arguments second.	
	self assert: #read equals: arg2 selector.
	self assert: 1 equals: arg2 arguments size.
	self assert: 15 equals: arg2 arguments first value.