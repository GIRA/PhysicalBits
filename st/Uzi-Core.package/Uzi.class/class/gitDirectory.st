config-paths
gitDirectory
	^ gitDirectory ifNil: [Error signal: 'Git directory was not configured!']