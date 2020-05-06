visiting
visitForNode: node
	self writeLine: '(ast/for-node'.
	self indent: 1
		during: [
			self indent; writeLine: '"{1}"' format: { node counter name };
				indent; visit: node start; writeLine: '';
				indent; visit: node stop; writeLine: ''.
			node step
				ifNil: [self indent; writeLine: '(ast/literal-number-node 1)']
				ifNotNil: [:step | self indent; visit: step; writeLine: ''].
			self indent; visit: node body;
				write: ')']