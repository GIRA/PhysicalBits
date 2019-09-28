as yet unclassified
compile: anUziParseNode
	^ super compile: (self removeDeadCode: anUziParseNode)