visiting
visitImportNode: node
	node initializationBlock
		ifNil: [self write: '(ast/import-node "{1}" "{2}")' format: { node alias . node path }]
		ifNotNil: [:block |
			self writeLine: '(ast/import-node "{1}" "{2}"' format: { node alias . node path }.
			self indent: 1
				during: [self indent; visit: block; write: ')']]