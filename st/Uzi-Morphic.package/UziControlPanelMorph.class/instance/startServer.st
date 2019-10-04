server
startServer 
	server ifNotNil: [^ self].
	server := UziServer controller: controller.
	[self openBrowser] on: Error do: [:ign | "Do nothing"]