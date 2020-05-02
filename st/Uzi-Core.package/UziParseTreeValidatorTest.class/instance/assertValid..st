utils
assertValid: src
	| check |
	UziCljTestReportGenerator current register: src valid: true test: thisContext receiver selector.
	check := self check: src.
	self assert: check isEmpty.