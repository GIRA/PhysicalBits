tests
testScriptCallWithoutParameters
	| ast pp call |
	pp := parser.
	ast := pp parse: '@main [a := @foo value]'.
	call := ast scripts first body statements first right.
	self assert: #foo equals: call script name