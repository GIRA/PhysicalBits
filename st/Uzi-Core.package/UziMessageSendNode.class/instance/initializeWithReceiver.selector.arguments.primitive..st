initialize
initializeWithReceiver: anUziParseNode
selector: aSymbol
arguments: anArrayOfUziParseNodes
primitive: anUziPrimitive

	receiver := anUziParseNode.
	selector := aSymbol.
	arguments := anArrayOfUziParseNodes.
	primitive := anUziPrimitive.
	self initialize