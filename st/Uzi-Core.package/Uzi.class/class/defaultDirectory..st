config-paths
defaultDirectory: aFileDirectory
	"
	Uzi defaultDirectory: (FileDirectory on: 'D:\UAI\GIRA\Uzi')
	"
	| dir index parts |
	parts := aFileDirectory pathParts.
	dir := index := nil.
	parts withIndexDo: [:part :i |
		dir ifNil: [dir := FileDirectory on: part]
			ifNotNil: [dir := dir / part].
		FileDirectory default = dir
			ifTrue: [index := i]].
	index ifNil: [defaultPath := parts asArray]
		ifNotNil: [defaultPath := (parts allButFirst: index) asArray].
	^ self defaultDirectory 