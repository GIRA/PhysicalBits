utils
assertValid: src
	| check |
	check := self check: src.
	self assert: check isEmpty.