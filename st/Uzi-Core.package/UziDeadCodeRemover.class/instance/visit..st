visiting
visit: anUziParseNode
	path addFirst: anUziParseNode.
	[^ anUziParseNode accept: self] 
		ensure: [path removeFirst]