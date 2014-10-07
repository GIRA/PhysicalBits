as yet unclassified
testOverflow
	| stack |
	stack := UziStack new.
	stack push: 1.
	self deny: stack overflow.
	98 timesRepeat: [stack push: 1].
	self deny: stack overflow.
	stack push: 1.
	self assert: stack overflow 