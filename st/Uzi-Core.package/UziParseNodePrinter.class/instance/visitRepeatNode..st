visiting
visitRepeatNode: node
	self write: 'repeat ';
		visit: node times;
		write: ' ';
		visit: node body