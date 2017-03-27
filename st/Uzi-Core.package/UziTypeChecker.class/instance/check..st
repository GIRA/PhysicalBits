checking
check: anUziParseNode 
	program := anUziParseNode.
	errors := OrderedCollection new.
	self visit: anUziParseNode.
	^ errors