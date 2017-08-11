checking
check: anUziParseNode
	errors := OrderedCollection new.
	self visit: anUziParseNode.
	^ errors