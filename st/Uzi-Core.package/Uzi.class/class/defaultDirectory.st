config-paths
defaultDirectory
	"This is the directory on the file system in which all the Uzi files are stored. Tipically it's where you cloned
	the git repository. In order for the system to work this directory needs to be correctly configured."
	| default |
	defaultPath ifNil: [Error signal: 'Default directory is not configured!'].
	
	"Try relative path first"
	default := defaultPath 
		inject: FileDirectory default 
		into: [:dir :next | dir / next].
	default exists ifTrue: [^ default].
	
	"The relative path doesn't exist. Try absolute now"
	^ defaultPath allButFirst 
		inject: (FileDirectory on: defaultPath first) 
		into: [:dir :next | dir / next]