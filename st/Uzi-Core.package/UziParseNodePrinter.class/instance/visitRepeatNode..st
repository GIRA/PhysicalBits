visiting
visitRepeatNode: node
	self saveIntervalFor: node
		during: [
			self write: 'repeat ';
				visit: node times;
				write: ' ';
				visit: node body]