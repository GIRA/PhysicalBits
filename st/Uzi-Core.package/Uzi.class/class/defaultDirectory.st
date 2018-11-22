config-paths
defaultDirectory
	"This is the directory on the file system in which all the Uzi files are stored. Tipically it's where you cloned
	the git repository. In order for the system to work this directory needs to be correctly configured."
	^ defaultDirectory ifNil: [Error signal: 'Default directory is not configured!']