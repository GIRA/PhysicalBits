config-paths
defaultPath
	"Internal representation of the default directory. I don't know if there is a better way of doing this
	but I wanted to store the path relative to the working directory in a portable way. So instead of 
	storing the actual directory I store the path parts and build the directory when requested."
	^ defaultPath