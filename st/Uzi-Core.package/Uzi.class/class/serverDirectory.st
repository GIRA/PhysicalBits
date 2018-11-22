config-paths
serverDirectory
	"This directory stores all the files needed for the web tools (javascript, html, and so on).
	Normally, it's relative to the default directory so there is no need to configure it. 
	However, you can overwrite it if you need."
	^ serverDirectory ifNil: [self defaultDirectory / 'web']