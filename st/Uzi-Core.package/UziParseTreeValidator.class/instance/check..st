checking
check: anUziParseNode
	errors := OrderedCollection new.
	path := OrderedCollection new.
	self visit: anUziParseNode.
	^ errors