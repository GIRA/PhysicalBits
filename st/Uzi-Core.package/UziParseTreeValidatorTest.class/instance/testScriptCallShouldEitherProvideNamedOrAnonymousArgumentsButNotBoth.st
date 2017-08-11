tests
testScriptCallShouldEitherProvideNamedOrAnonymousArgumentsButNotBoth
	self assertInvalid: '
	func foo(a, b, c) { return a * b + c; }
	task main() running {
		foo(1, 2, c: 3);
	}'