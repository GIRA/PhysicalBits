config-paths
serverDirectory
	^ serverDirectory ifNil: [self gitDirectory / 'web']