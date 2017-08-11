tests
testScriptReferenceShouldReferenceExistingScript
	self assertInvalid: 'task foo() { stop main; }'