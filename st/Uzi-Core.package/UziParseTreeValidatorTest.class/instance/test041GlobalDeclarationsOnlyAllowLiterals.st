tests
test041GlobalDeclarationsOnlyAllowLiterals
	self assertInvalid: 'var a = 3+4; task foo() {}'.
	self assertValid: 'var a = 3; task foo() {}'.
	self assertValid: 'var a = A3; task foo() {}'.
	self assertValid: 'var a = D3; task foo() {}'.