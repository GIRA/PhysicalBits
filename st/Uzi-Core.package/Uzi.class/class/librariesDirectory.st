config
librariesDirectory
	^ librariesDirectory ifNil: [self defaultLibrariesDirectory]