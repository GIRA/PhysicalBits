tests
test020ScriptWithArguments
	| pp ast |
	pp := parser script end.
	ast := pp parse: 'task foo(a, b) ticking 1/s {}'.
	self assert: 2 equals: ast arguments size.
	self assert: #a equals: ast arguments first name.
	self assert: #b equals: ast arguments second name.