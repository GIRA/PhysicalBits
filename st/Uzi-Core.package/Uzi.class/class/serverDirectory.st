config-paths
serverDirectory
	^ serverDirectory ifNil: [self defaultDirectory / 'web']