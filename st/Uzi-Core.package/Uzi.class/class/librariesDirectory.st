config-paths
librariesDirectory
	"This directory stores all the Uzi libraries so that users can import them in their programs.
	Normally, it's relative to the default directory so there is no need to configure it. 
	However, you can overwrite it if you need."
	^ librariesDirectory ifNil: [self defaultDirectory / 'uzi' / 'libraries']