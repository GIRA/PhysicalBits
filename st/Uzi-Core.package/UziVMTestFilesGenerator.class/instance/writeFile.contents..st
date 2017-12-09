utils
writeFile: fileName contents: bytes
	(FileDirectory on: self class outputPath)
		forceNewFileNamed: fileName do: [:file |
			bytes do: [:b | file nextPutAll: b asString]
				separatedBy: [file nextPutAll: ', ']]