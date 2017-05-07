tests
testScriptCallShouldEitherProvideNamedOrAnonymousArgumentsButNotBoth
	self assertInvalid: '
@foo [:a :b :c | ^ a * b + c]
@main ticking [@foo value: 1 value: 2 c: 3]
'