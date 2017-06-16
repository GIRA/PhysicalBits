tests
test004CallWithoutArguments
	| pp ast |
	pp := parser call end.
	ast := pp parse: 'millis()'.
	self assert: #millis equals: ast selector.
	self assert: 0 equals: ast arguments size