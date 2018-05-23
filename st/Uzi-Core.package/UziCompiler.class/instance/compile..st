compiling
compile: anUziParseNode
	path := OrderedCollection new.
	variableNames := Dictionary new.
	^ self visit: anUziParseNode 