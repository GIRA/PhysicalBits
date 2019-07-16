stepping
startSteppingProcess
	process ifNotNil: [^ self].
	process := self fork: [self stepLoop] named: 'Step'