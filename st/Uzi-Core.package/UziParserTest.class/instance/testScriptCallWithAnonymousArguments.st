tests
testScriptCallWithAnonymousArguments
	| ast pp call |
	pp := parser.
	ast := pp parse: '@main [a := @foo value: 3 value: 4]'.
	call := ast scripts first body statements first right.
	self assert: #foo equals: call script name.
	self assert: call arguments size equals: 2.
	self assert: call arguments first key equals: #value.
	self assert: call arguments first value value equals: 3.
	self assert: call arguments second key equals: #value.
	self assert: call arguments second value value equals: 4.