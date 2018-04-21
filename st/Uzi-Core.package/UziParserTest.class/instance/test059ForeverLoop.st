tests
test059ForeverLoop
	| program script body |
	program := parser parse: '
		task main() running {
			forever {
				toggle(D13);
				delayMs(1000);
			}
		}'.
	script := program scriptNamed: 'main'.
	self assert: 1 equals: script body statements size.
	body := script body statements first body.
	self assert: 2 equals: body statements size.
	self assert: #toggle equals: body statements first selector.
	self assert: #delayMs equals: body statements second selector.