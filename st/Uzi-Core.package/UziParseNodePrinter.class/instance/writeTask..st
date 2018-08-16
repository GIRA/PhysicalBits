visiting
writeTask: node
	self indent; 
		write: 'task {1}() {2}'
		format: {
			node name. 
			node state.
		}.
	node tickingRate 
		ifNotNil: [:tr | self write: ' '; visit: tr].
	self write: ' ';
		visit: node body