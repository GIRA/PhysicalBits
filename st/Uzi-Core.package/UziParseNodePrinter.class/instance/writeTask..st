visiting
writeTask: node
	self indent; 
		write: 'task {1}()' format: { node name }.
	node state ~= #once
		ifTrue: [self write: ' {1}' format: { node state }].
	node tickingRate 
		ifNotNil: [:tr | self write: ' '; visit: tr].
	self write: ' ';
		visit: node body