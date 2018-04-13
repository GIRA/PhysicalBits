tests
test023YieldShouldNotBeUsedAsExpression
	self assertInvalid: '
	func yield () {
		return yield;
	}'.