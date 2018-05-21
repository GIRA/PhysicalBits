private
assert: expected equals: actual description: message
	self assert: expected = actual
		description: ('{1}. Expected: {2}. Actual: {3}'
			format: { message. expected. actual })