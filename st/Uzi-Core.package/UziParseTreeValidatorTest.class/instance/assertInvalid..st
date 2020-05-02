utils
assertInvalid: src
	| check |
	UziCljTestReportGenerator current register: src valid: false test: thisContext receiver selector.
	check := self check: src.
	self assert: check notEmpty.