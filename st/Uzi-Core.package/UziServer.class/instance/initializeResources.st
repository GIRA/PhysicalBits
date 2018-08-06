initialize-release
initializeResources
	self at: #web put: (RESTFileSystem directory: webDirectory);
		at: #uzi put: (UziResource on: self)