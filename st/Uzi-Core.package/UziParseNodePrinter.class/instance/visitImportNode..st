visiting
visitImportNode: node
	self saveIntervalFor: node
		during: [self 
			write: 'import {1} from ''{2}'''
			format: { node alias . node path }]