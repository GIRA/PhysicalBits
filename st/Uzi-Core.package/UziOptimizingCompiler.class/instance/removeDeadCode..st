as yet unclassified
removeDeadCode: anUziParseNode
	| rem |
	rem := UziDeadCodeRemover new.
	^ (rem applyOn: anUziParseNode) originalAST: anUziParseNode originalAST