config-paths
librariesDirectory
	^ librariesDirectory ifNil: [self gitDirectory / 'uzi' / 'libraries']