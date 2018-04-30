utils
writeFile: fileName contents: bytes
	self class outputDirectory
		forceNewFileNamed: fileName do: [:file |
			bytes do: [:b | file nextPutAll: b asString]
				separatedBy: [file nextPutAll: ', ']]