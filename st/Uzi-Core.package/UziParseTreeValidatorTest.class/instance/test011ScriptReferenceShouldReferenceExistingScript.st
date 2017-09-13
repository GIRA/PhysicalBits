tests
test011ScriptReferenceShouldReferenceExistingScript
	self assertInvalid: 'task foo() stopped { stop main; }'