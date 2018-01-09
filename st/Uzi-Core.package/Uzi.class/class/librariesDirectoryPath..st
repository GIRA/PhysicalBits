config
librariesDirectoryPath: aString
	"
	self librariesDirectoryPath: 'C:\UAI\GIRA\Uzi\uzi\libraries'
	"
	self librariesDirectory: (FileDirectory on: aString)