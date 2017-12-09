utils
writeFile: fileName contents: bytes
	(FileDirectory on: self class outputPath)
		fileNamed: fileName do: [:file |
			bytes do: [:b | file nextPutAll: b asString]
				separatedBy: [file nextPutAll: ', ']]