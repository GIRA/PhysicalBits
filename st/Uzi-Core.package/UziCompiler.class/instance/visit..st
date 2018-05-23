visiting-utils
visit: anUziNode
	path addFirst: anUziNode.
	[^ anUziNode accept: self] 
		ensure: [path removeFirst]