config-paths
librariesDirectory
	^ librariesDirectory ifNil: [self defaultDirectory / 'uzi' / 'libraries']