tests
testScriptReferenceShouldReferenceExistingScript
	self assertInvalid: 'task foo() stopped { stop main; }'