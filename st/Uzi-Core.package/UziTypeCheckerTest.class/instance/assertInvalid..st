utils
assertInvalid: src
	| check |
	check := self check: src.
	self assert: (check isPetitFailure or: [check notEmpty]).