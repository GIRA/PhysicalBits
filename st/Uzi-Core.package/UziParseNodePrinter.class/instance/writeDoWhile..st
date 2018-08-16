visiting
writeDoWhile: node
	self write: 'do ';
		visit: node body;
		write: ' while ';
		visit: node condition;
		write: ';'